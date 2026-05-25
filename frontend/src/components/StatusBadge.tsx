import { Badge } from 'react-bootstrap';

const map: Record<string, string> = {
  PENDING: 'warning',
  APPROVED: 'info',
  ACTIVE: 'success',
  CLOSED: 'secondary',
  DUE: 'warning',
  PAID: 'success',
  PARTIAL: 'primary',
  OVERDUE: 'danger',
};

export default function StatusBadge({ status }: { status: string }) {
  return <Badge bg={map[status] ?? 'secondary'}>{status}</Badge>;
}
