import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { FormEvent, useState } from 'react';
import { Button, Card, Col, Form, Row, Table } from 'react-bootstrap';
import { useParams } from 'react-router-dom';
import { api, formatInr } from '../../api/client';
import EmptyState from '../../components/EmptyState';
import LoadingBlock from '../../components/LoadingBlock';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import { useConfirm } from '../../context/ConfirmContext';
import { getErrorMessage } from '../../lib/errors';
import { notify } from '../../lib/toast';

export default function BorrowerLoanDetail() {
  const { id } = useParams();
  const qc = useQueryClient();
  const { confirm } = useConfirm();
  const [amount, setAmount] = useState('');
  const [paymentRef, setPaymentRef] = useState('');
  const { data: schedule = [], isLoading } = useQuery({
    queryKey: ['borrower-schedule', id],
    queryFn: () => api.get(`/borrower/loans/${id}/schedule`).then((r) => r.data),
    enabled: !!id,
  });

  const firstDue = schedule.find((e: { status: string }) => e.status === 'DUE' || e.status === 'PARTIAL') as
    | { emiAmount: number }
    | undefined;

  const repay = useMutation({
    mutationFn: () =>
      api.post(
        `/borrower/loans/${id}/repay`,
        { amount: Number(amount), paymentRef },
        { headers: { 'Idempotency-Key': crypto.randomUUID() } },
      ),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['borrower-schedule', id] });
      qc.invalidateQueries({ queryKey: ['my-loans'] });
      setAmount('');
      setPaymentRef('');
      notify.success('Payment recorded successfully');
    },
    onError: (err) => notify.error(getErrorMessage(err, 'Payment failed')),
  });

  const fillEmiAmount = () => {
    if (firstDue) setAmount(String(firstDue.emiAmount));
  };

  const onPay = async (e: FormEvent) => {
    e.preventDefault();
    const ok = await confirm({
      title: 'Confirm payment',
      message: `Pay ${formatInr(Number(amount))} with reference "${paymentRef}"?`,
      confirmText: 'Pay EMI',
      variant: 'success',
    });
    if (ok) repay.mutate();
  };

  return (
    <>
      <PageHeader
        title="Schedule & Pay"
        summary="Pay EMI installments. Amount is allocated to oldest due EMIs first."
        steps={[
          'Enter amount (use full EMI to avoid PARTIAL status).',
          'Enter payment reference (mock UPI id).',
          'Confirm and submit.',
        ]}
        backTo={{ label: 'My loans', path: '/borrower/dashboard' }}
        tip={firstDue ? `Next due EMI: ${formatInr(firstDue.emiAmount)}` : undefined}
      />
      <Card className="mb-4">
        <Card.Header>Make a payment</Card.Header>
        <Card.Body>
          <Form onSubmit={onPay}>
            <Row className="g-3 align-items-end">
              <Col md={4}>
                <Form.Label>Amount (₹)</Form.Label>
                <Form.Control type="number" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required />
                {firstDue && (
                  <Button variant="link" size="sm" className="p-0 mt-1" type="button" onClick={fillEmiAmount}>
                    Use full EMI ({formatInr(firstDue.emiAmount)})
                  </Button>
                )}
              </Col>
              <Col md={4}>
                <Form.Label>Payment reference</Form.Label>
                <Form.Control placeholder="MOCK-UPI-001" value={paymentRef} onChange={(e) => setPaymentRef(e.target.value)} required />
              </Col>
              <Col md={4}>
                <Button type="submit" variant="success" className="w-100" disabled={repay.isPending}>
                  {repay.isPending ? 'Processing…' : (
                    <>
                      <i className="bi bi-credit-card me-1" />
                      Pay EMI
                    </>
                  )}
                </Button>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>
      <Card>
        <Card.Header>EMI schedule</Card.Header>
        <Card.Body className="p-0">
          {isLoading ? (
            <LoadingBlock />
          ) : schedule.length === 0 ? (
            <div className="p-4">
              <EmptyState message="No installments found." />
            </div>
          ) : (
            <Table responsive hover className="mb-0">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Due</th>
                  <th>EMI</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {schedule.map((e: { id: string; installmentNo: number; dueDate: string; emiAmount: number; status: string }) => (
                  <tr key={e.id}>
                    <td>{e.installmentNo}</td>
                    <td>{e.dueDate}</td>
                    <td>{formatInr(e.emiAmount)}</td>
                    <td>
                      <StatusBadge status={e.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}
        </Card.Body>
      </Card>
    </>
  );
}
