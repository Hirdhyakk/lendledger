import { useQuery } from '@tanstack/react-query';
import { Card, Table } from 'react-bootstrap';
import { useParams } from 'react-router-dom';
import { api, formatInr } from '../../api/client';
import EmptyState from '../../components/EmptyState';
import LoadingBlock from '../../components/LoadingBlock';
import PageHeader from '../../components/PageHeader';

export default function BorrowerStatement() {
  const { id } = useParams();
  const { data, isLoading, isError } = useQuery({
    queryKey: ['statement', id],
    queryFn: () => api.get(`/borrower/loans/${id}/statement`).then((r) => r.data),
    enabled: !!id,
  });

  const ledger = data?.ledger ?? [];
  const repayments = data?.repayments ?? [];

  return (
    <>
      <PageHeader
        title="Account statement"
        summary="Ledger from payment-service: DISBURSE when funds released, REPAYMENT when you pay."
        backTo={{ label: 'My loans', path: '/borrower/dashboard' }}
      />
      {isError && <div className="alert alert-danger">Could not load statement.</div>}
      {isLoading ? (
        <LoadingBlock />
      ) : (
        <>
          <Card className="mb-4">
            <Card.Header>Ledger entries</Card.Header>
            <Card.Body className="p-0">
              {ledger.length === 0 ? (
                <div className="p-4">
                  <EmptyState message="No ledger entries yet." icon="journal" />
                </div>
              ) : (
                <Table responsive hover className="mb-0">
                  <thead>
                    <tr>
                      <th>Type</th>
                      <th>Amount</th>
                      <th>Reference</th>
                      <th>When</th>
                    </tr>
                  </thead>
                  <tbody>
                    {ledger.map((e: { id: string; entryType: string; amount: number; reference: string; createdAt: string }) => (
                      <tr key={e.id}>
                        <td>
                          <span className="fw-semibold">{e.entryType}</span>
                        </td>
                        <td>{formatInr(e.amount)}</td>
                        <td>{e.reference}</td>
                        <td>{new Date(e.createdAt).toLocaleString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
          <Card>
            <Card.Header>Repayments</Card.Header>
            <Card.Body className="p-0">
              {repayments.length === 0 ? (
                <div className="p-4">
                  <EmptyState message="No repayments yet." hint="Pay an EMI from Schedule & Pay." />
                </div>
              ) : (
                <Table responsive hover className="mb-0">
                  <thead>
                    <tr>
                      <th>Amount</th>
                      <th>Ref</th>
                      <th>When</th>
                    </tr>
                  </thead>
                  <tbody>
                    {repayments.map((r: { id: string; amount: number; paymentRef: string; createdAt: string }) => (
                      <tr key={r.id}>
                        <td>{formatInr(r.amount)}</td>
                        <td>{r.paymentRef}</td>
                        <td>{new Date(r.createdAt).toLocaleString()}</td>
                      </tr>
                    ))}
                  </tbody>
                </Table>
              )}
            </Card.Body>
          </Card>
        </>
      )}
    </>
  );
}
