import { Card } from 'react-bootstrap';

export default function EmptyState({ icon = 'inbox', message, hint }: { icon?: string; message: string; hint?: string }) {
  return (
    <Card className="text-center py-5 border-dashed">
      <Card.Body>
        <i className={`bi bi-${icon} display-4 text-muted mb-3 d-block`} />
        <p className="text-muted mb-1">{message}</p>
        {hint && <p className="small text-secondary mb-0">{hint}</p>}
      </Card.Body>
    </Card>
  );
}
