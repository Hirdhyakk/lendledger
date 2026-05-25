import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { FormEvent, useState } from 'react';
import { Alert, Button, Card, Col, Form, Row, Table } from 'react-bootstrap';
import { api } from '../../api/client';
import EmptyState from '../../components/EmptyState';
import LoadingBlock from '../../components/LoadingBlock';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';
import { getErrorMessage } from '../../lib/errors';
import { notify } from '../../lib/toast';

export default function AdminBorrowers() {
  const qc = useQueryClient();
  const { data: borrowers = [], isLoading, isError } = useQuery({
    queryKey: ['borrowers'],
    queryFn: () => api.get('/admin/borrowers').then((r) => r.data),
  });
  const [form, setForm] = useState({
    email: '',
    password: 'password',
    fullName: '',
    phone: '',
    address: '',
    panMasked: '',
  });

  const create = useMutation({
    mutationFn: () => api.post('/admin/borrowers', form),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['borrowers'] });
      setForm({ email: '', password: 'password', fullName: '', phone: '', address: '', panMasked: '' });
      notify.success('Borrower created. They can log in with email and password (default: password).');
    },
    onError: (err) => notify.error(getErrorMessage(err, 'Could not create borrower')),
  });

  const onSubmit = (e: FormEvent) => {
    e.preventDefault();
    create.mutate();
  };

  return (
    <>
      <PageHeader
        title="Borrowers"
        summary="Register people who can borrow. Each borrower gets a login for the borrower portal."
        steps={[
          'Enter email and full name (required).',
          'Click Create borrower — default password is "password".',
          'Go to Loans and select this person when creating a loan.',
        ]}
        tip="Demo: borrower1@lendledger.local / password"
      />
      {isError && <Alert variant="danger">Failed to load borrowers.</Alert>}
      <Card className="mb-4">
        <Card.Header>
          <i className="bi bi-person-plus me-2" />
          New borrower
        </Card.Header>
        <Card.Body>
          <Form onSubmit={onSubmit}>
            <Row className="g-3">
              <Col md={6}>
                <Form.Label>Email (login)</Form.Label>
                <Form.Control type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
              </Col>
              <Col md={6}>
                <Form.Label>Full name</Form.Label>
                <Form.Control value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} required />
              </Col>
              <Col md={6}>
                <Form.Label>Phone</Form.Label>
                <Form.Control value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
              </Col>
              <Col md={6}>
                <Form.Label>PAN (masked)</Form.Label>
                <Form.Control value={form.panMasked} onChange={(e) => setForm({ ...form, panMasked: e.target.value })} />
              </Col>
              <Col md={12}>
                <Form.Label>Address</Form.Label>
                <Form.Control value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} />
              </Col>
              <Col md={12}>
                <Button type="submit" variant="primary" disabled={create.isPending}>
                  {create.isPending ? 'Creating…' : (
                    <>
                      <i className="bi bi-check-lg me-1" />
                      Create borrower
                    </>
                  )}
                </Button>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>
      <Card>
        <Card.Header>All borrowers ({borrowers.length})</Card.Header>
        <Card.Body className="p-0">
          {isLoading ? (
            <LoadingBlock />
          ) : borrowers.length === 0 ? (
            <div className="p-4">
              <EmptyState message="No borrowers yet." hint="Create one above, then go to Loans." />
            </div>
          ) : (
            <Table responsive hover className="mb-0">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {borrowers.map((b: { id: string; fullName: string; email: string; status: string }) => (
                  <tr key={b.id}>
                    <td className="fw-medium">{b.fullName}</td>
                    <td>{b.email}</td>
                    <td>
                      <StatusBadge status={b.status} />
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
