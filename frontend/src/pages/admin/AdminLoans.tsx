import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { FormEvent, useState } from 'react';
import { Alert, Badge, Button, Card, Col, Form, Row, Table } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { api, formatInr } from '../../api/client';
import EmptyState from '../../components/EmptyState';
import LoadingBlock from '../../components/LoadingBlock';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import { useConfirm } from '../../context/ConfirmContext';
import { getErrorMessage } from '../../lib/errors';
import { notify } from '../../lib/toast';

export default function AdminLoans() {
  const qc = useQueryClient();
  const { confirm } = useConfirm();
  const { data: loans = [], isLoading } = useQuery({
    queryKey: ['loans'],
    queryFn: () => api.get('/admin/loans').then((r) => r.data),
  });
  const { data: borrowers = [] } = useQuery({
    queryKey: ['borrowers'],
    queryFn: () => api.get('/admin/borrowers').then((r) => r.data),
  });
  const [form, setForm] = useState({ borrowerId: '', principal: '50000', annualRate: '12', tenureMonths: '6' });

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['loans'] });
    qc.invalidateQueries({ queryKey: ['admin-stats'] });
  };

  const create = useMutation({
    mutationFn: () =>
      api.post('/admin/loans', {
        borrowerId: form.borrowerId,
        principal: Number(form.principal),
        annualRate: Number(form.annualRate),
        tenureMonths: Number(form.tenureMonths),
      }),
    onSuccess: () => {
      invalidate();
      notify.success('Loan created (PENDING). Approve to generate the EMI schedule.');
    },
    onError: (err) => notify.error(getErrorMessage(err, 'Could not create loan')),
  });

  const approve = useMutation({
    mutationFn: (id: string) => api.post(`/admin/loans/${id}/approve`),
    onSuccess: () => {
      invalidate();
      notify.success('Loan approved. EMI schedule generated — you can disburse next.');
    },
    onError: (err) => notify.error(getErrorMessage(err, 'Approve failed')),
  });

  const disburse = useMutation({
    mutationFn: (id: string) => api.post(`/admin/loans/${id}/disburse`),
    onSuccess: () => {
      invalidate();
      notify.success('Loan disbursed and ACTIVE. Borrower can pay EMIs now.');
    },
    onError: (err) => notify.error(getErrorMessage(err, 'Disburse failed — is payment-service running?')),
  });

  const handleApprove = async (id: string) => {
    const ok = await confirm({
      title: 'Approve loan?',
      message: 'This generates the EMI schedule (reducing balance). You can disburse funds after approval.',
      confirmText: 'Approve',
      variant: 'primary',
    });
    if (ok) approve.mutate(id);
  };

  const handleDisburse = async (id: string) => {
    const ok = await confirm({
      title: 'Disburse loan?',
      message: 'Funds will be released, a ledger entry recorded, and the borrower can start repayments. This cannot be undone in the demo.',
      confirmText: 'Disburse funds',
      variant: 'warning',
    });
    if (ok) disburse.mutate(id);
  };

  return (
    <>
      <PageHeader
        title="Loans"
        summary="Lifecycle: PENDING → APPROVED → ACTIVE. Only ACTIVE loans appear for borrower repayment."
        steps={[
          'Select borrower, principal, rate %, and tenure.',
          'Create → Approve → Disburse.',
          'Click loan ID to view EMI schedule.',
        ]}
      />
      <div className="d-flex flex-wrap gap-2 mb-3">
        <Badge bg="warning" text="dark">
          PENDING
        </Badge>
        <span className="small text-muted">awaiting approval</span>
        <Badge bg="info">APPROVED</Badge>
        <span className="small text-muted">ready to disburse</span>
        <Badge bg="success">ACTIVE</Badge>
        <span className="small text-muted">borrower can pay</span>
      </div>
      <Card className="mb-4">
        <Card.Header>Create loan</Card.Header>
        <Card.Body>
          <Form
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              create.mutate();
            }}
          >
            <Row className="g-3 align-items-end">
              <Col md={4}>
                <Form.Label>Borrower</Form.Label>
                <Form.Select value={form.borrowerId} onChange={(e) => setForm({ ...form, borrowerId: e.target.value })} required>
                  <option value="">Select borrower</option>
                  {borrowers.map((b: { id: string; fullName: string }) => (
                    <option key={b.id} value={b.id}>
                      {b.fullName}
                    </option>
                  ))}
                </Form.Select>
              </Col>
              <Col md={2}>
                <Form.Label>Principal (₹)</Form.Label>
                <Form.Control type="number" value={form.principal} onChange={(e) => setForm({ ...form, principal: e.target.value })} />
              </Col>
              <Col md={2}>
                <Form.Label>Rate % / yr</Form.Label>
                <Form.Control type="number" value={form.annualRate} onChange={(e) => setForm({ ...form, annualRate: e.target.value })} />
              </Col>
              <Col md={2}>
                <Form.Label>Months</Form.Label>
                <Form.Control type="number" value={form.tenureMonths} onChange={(e) => setForm({ ...form, tenureMonths: e.target.value })} />
              </Col>
              <Col md={2}>
                <Button type="submit" variant="primary" className="w-100" disabled={create.isPending}>
                  Create
                </Button>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>
      <Card>
        <Card.Header>All loans</Card.Header>
        <Card.Body className="p-0">
          {isLoading ? (
            <LoadingBlock />
          ) : loans.length === 0 ? (
            <div className="p-4">
              <EmptyState message="No loans yet." hint="Create a borrower first." />
            </div>
          ) : (
            <Table responsive hover className="mb-0">
              <thead>
                <tr>
                  <th>Loan ID</th>
                  <th>Principal</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {loans.map((l: { id: string; principal: number; status: string }) => (
                  <tr key={l.id}>
                    <td>
                      <Link to={`/admin/loans/${l.id}`} className="font-monospace text-decoration-none">
                        {l.id.slice(0, 8)}…
                      </Link>
                    </td>
                    <td>{formatInr(l.principal)}</td>
                    <td>
                      <StatusBadge status={l.status} />
                    </td>
                    <td>
                      {l.status === 'PENDING' && (
                        <Button size="sm" variant="warning" className="me-1" onClick={() => handleApprove(l.id)} disabled={approve.isPending}>
                          Approve
                        </Button>
                      )}
                      {l.status === 'APPROVED' && (
                        <Button size="sm" variant="success" onClick={() => handleDisburse(l.id)} disabled={disburse.isPending}>
                          Disburse
                        </Button>
                      )}
                      {l.status === 'ACTIVE' && <span className="small text-muted">Borrower can pay</span>}
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
