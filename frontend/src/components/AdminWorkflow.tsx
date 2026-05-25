import { Card, Col, Row } from 'react-bootstrap';
import { Link } from 'react-router-dom';

const steps = [
  { n: 1, label: 'Borrowers', path: '/admin/borrowers', icon: 'person-plus', desc: 'Register people who can borrow' },
  { n: 2, label: 'Loans', path: '/admin/loans', icon: 'file-earmark-plus', desc: 'Create → Approve → Disburse' },
  { n: 3, label: 'EMI schedule', path: '/admin/loans', icon: 'calendar3', desc: 'Open loan ID after approve' },
  { n: 4, label: 'Borrower pays', path: '/login', icon: 'credit-card', desc: 'Borrower login → Pay EMI' },
  { n: 5, label: 'Reports', path: '/admin/reports', icon: 'graph-up', desc: 'Collections & overdue' },
];

export default function AdminWorkflow() {
  return (
    <Card className="mb-4">
      <Card.Header>
        <i className="bi bi-diagram-3 me-2" />
        How LendLedger works
      </Card.Header>
      <Card.Body>
        <p className="text-muted small mb-3">
          Register a borrower → create a loan → approve (EMI schedule) → disburse (ledger) → borrower repays →
          track collections.
        </p>
        <Row className="g-3">
          {steps.map((s) => (
            <Col key={s.n} xs={12} sm={6} lg>
              <Link to={s.path} className="workflow-step card h-100 p-3">
                <span className="badge bg-primary rounded-pill mb-2 align-self-start">Step {s.n}</span>
                <div className="fw-semibold">
                  <i className={`bi bi-${s.icon} me-2 text-primary`} />
                  {s.label}
                </div>
                <div className="small text-muted mt-1">{s.desc}</div>
              </Link>
            </Col>
          ))}
        </Row>
      </Card.Body>
    </Card>
  );
}
