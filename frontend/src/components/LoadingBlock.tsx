import { Spinner } from 'react-bootstrap';

export default function LoadingBlock({ message = 'Loading…' }: { message?: string }) {
  return (
    <div className="text-center py-5">
      <Spinner animation="border" variant="primary" role="status" />
      <p className="text-muted mt-3 mb-0">{message}</p>
    </div>
  );
}
