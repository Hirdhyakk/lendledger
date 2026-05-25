import { useQuery } from '@tanstack/react-query';
import { Card, Table } from 'react-bootstrap';
import { useParams } from 'react-router-dom';
import { api, formatInr } from '../../api/client';
import EmptyState from '../../components/EmptyState';
import LoadingBlock from '../../components/LoadingBlock';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';

export default function AdminLoanDetail() {
  const { id } = useParams();
  const { data: schedule = [], isLoading } = useQuery({
    queryKey: ['schedule', id],
    queryFn: () => api.get(`/admin/loans/${id}/schedule`).then((r) => r.data),
    enabled: !!id,
  });

  return (
    <>
      <PageHeader
        title="EMI schedule"
        summary="Monthly installments generated at approval (reducing-balance EMI)."
        steps={['DUE = unpaid', 'PAID = fully paid', 'PARTIAL = underpaid']}
        backTo={{ label: 'Loans', path: '/admin/loans' }}
        tip={`Loan ID: ${id}`}
      />
      <Card>
        <Card.Header>Installments</Card.Header>
        <Card.Body className="p-0">
          {isLoading ? (
            <LoadingBlock />
          ) : schedule.length === 0 ? (
            <div className="p-4">
              <EmptyState message="No EMI rows." hint="Approve the loan to generate the schedule." />
            </div>
          ) : (
            <Table responsive hover className="mb-0">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Due date</th>
                  <th>EMI</th>
                  <th>Paid</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {schedule.map((e: { id: string; installmentNo: number; dueDate: string; emiAmount: number; paidAmount: number; status: string }) => (
                  <tr key={e.id}>
                    <td>{e.installmentNo}</td>
                    <td>{e.dueDate}</td>
                    <td>{formatInr(e.emiAmount)}</td>
                    <td>{formatInr(e.paidAmount)}</td>
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
