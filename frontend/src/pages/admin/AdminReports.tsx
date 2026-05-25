import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { Button, Card, Col, Form, Row, Table } from 'react-bootstrap';
import { api, formatInr } from '../../api/client';
import EmptyState from '../../components/EmptyState';
import LoadingBlock from '../../components/LoadingBlock';
import PageHeader from '../../components/PageHeader';
import { notify } from '../../lib/toast';

export default function AdminReports() {
  const [from, setFrom] = useState(new Date().toISOString().slice(0, 10));
  const [to, setTo] = useState(new Date().toISOString().slice(0, 10));

  const { data: collections, isLoading: loadingColl } = useQuery({
    queryKey: ['collections', from, to],
    queryFn: () => api.get('/admin/reports/collections', { params: { from, to } }).then((r) => r.data),
  });

  const { data: overdue = [], isLoading: loadingOd } = useQuery({
    queryKey: ['overdue'],
    queryFn: () => api.get('/admin/reports/overdue').then((r) => r.data),
  });

  const exportCsv = () => {
    if (overdue.length === 0) {
      notify.error('No overdue rows to export');
      return;
    }
    const rows = [
      ['LoanId', 'EMI', 'Due', 'Borrower'],
      ...overdue.map((o: { loanId: string; installmentNo: number; dueDate: string; borrowerName: string }) => [
        o.loanId,
        o.installmentNo,
        o.dueDate,
        o.borrowerName,
      ]),
    ];
    const csv = rows.map((r) => r.join(',')).join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'overdue.csv';
    a.click();
    notify.success('CSV downloaded');
  };

  return (
    <>
      <PageHeader
        title="Reports"
        summary="Collections by date range and overdue EMI list."
        steps={['Set date range for collections', 'Export overdue CSV when needed']}
      />
      <Card className="mb-4">
        <Card.Body>
          <Row className="g-3 align-items-end">
            <Col md={3}>
              <Form.Label>From</Form.Label>
              <Form.Control type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
            </Col>
            <Col md={3}>
              <Form.Label>To</Form.Label>
              <Form.Control type="date" value={to} onChange={(e) => setTo(e.target.value)} />
            </Col>
            <Col md={4}>
              <p className="text-muted small mb-1">Total collected</p>
              <h3 className="mb-0 text-success fw-bold">
                {loadingColl ? '…' : formatInr(collections?.totalCollected ?? 0)}
              </h3>
            </Col>
          </Row>
        </Card.Body>
      </Card>
      <Card>
        <Card.Header className="d-flex justify-content-between align-items-center">
          <span>Overdue EMIs ({overdue.length})</span>
          <Button size="sm" variant="outline-dark" onClick={exportCsv}>
            <i className="bi bi-download me-1" />
            Export CSV
          </Button>
        </Card.Header>
        <Card.Body className="p-0">
          {loadingOd ? (
            <LoadingBlock />
          ) : overdue.length === 0 ? (
            <div className="p-4">
              <EmptyState message="No overdue EMIs." hint="Normal for new loans with future due dates." icon="check-circle" />
            </div>
          ) : (
            <Table responsive hover className="mb-0">
              <thead>
                <tr>
                  <th>Borrower</th>
                  <th>Due</th>
                  <th>EMI</th>
                </tr>
              </thead>
              <tbody>
                {overdue.map((o: { loanId: string; borrowerName: string; dueDate: string; emiAmount: number }) => (
                  <tr key={o.loanId + o.dueDate}>
                    <td>{o.borrowerName}</td>
                    <td>{o.dueDate}</td>
                    <td>{formatInr(o.emiAmount)}</td>
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
