import { Alert, Breadcrumb } from 'react-bootstrap';
import { Link } from 'react-router-dom';

type Props = {
  title: string;
  summary: string;
  steps?: string[];
  tip?: string;
  backTo?: { label: string; path: string };
};

export default function PageHeader({ title, summary, steps, tip, backTo }: Props) {
  return (
    <div className="mb-4">
      {backTo && (
        <Breadcrumb className="mb-2">
          <Breadcrumb.Item as={Link} to={backTo.path}>
            {backTo.label}
          </Breadcrumb.Item>
          <Breadcrumb.Item active>{title}</Breadcrumb.Item>
        </Breadcrumb>
      )}
      <h1 className="h3 fw-bold text-dark mb-1">{title}</h1>
      <p className="text-muted mb-0">{summary}</p>
      {steps && steps.length > 0 && (
        <Alert variant="info" className="mt-3 mb-0 border-0">
          <Alert.Heading className="h6 mb-2">
            <i className="bi bi-lightbulb me-2" />
            What to do on this page
          </Alert.Heading>
          <ol className="mb-0 ps-3 small">
            {steps.map((s) => (
              <li key={s} className="mb-1">
                {s}
              </li>
            ))}
          </ol>
        </Alert>
      )}
      {tip && (
        <p className="small text-secondary mt-2 mb-0">
          <i className="bi bi-info-circle me-1" />
          {tip}
        </p>
      )}
    </div>
  );
}
