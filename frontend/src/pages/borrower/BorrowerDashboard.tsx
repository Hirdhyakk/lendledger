import { useQuery } from '@tanstack/react-query';
import { Card, Col, Row } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import { api, formatInr } from '../../api/client';
import EmptyState from '../../components/EmptyState';
import LoadingBlock from '../../components/LoadingBlock';
import PageHeader from '../../components/PageHeader';
import StatusBadge from '../../components/StatusBadge';

export default function BorrowerDashboard() {
  const { data: loans = [], isLoading, isError } = useQuery({
    queryKey: ['my-loans'],
    queryFn: () => api.get('/borrower/loans').then((r) => r.data),
  });

  return (
    <>
      <PageHeader
        title="My loans"
        summary="Active loans after admin disbursement. Pay EMIs or view your statement."
        steps={['Schedule & Pay — submit repayment', 'Statement — ledger history']}
        tip="If empty, ask admin to create and disburse a loan for your email."
      />
      {isError && <div className="alert alert-danger">Could not load loans.</div>}
      {isLoading ? (
        <LoadingBlock />
      ) : loans.length === 0 ? (
        <EmptyState
          message="No active loans"
          hint="Admin must create a loan for you, approve it, and disburse funds."
          icon="wallet2"
        />
      ) : (
        <Row className="g-3">
          {loans.map((l: { id: string; principal: number; status: string; outstandingPrincipal: number }) => (
            <Col key={l.id} md={12} lg={6}>
              <Card className="h-100">
                <Card.Body>
                  <div className="d-flex justify-content-between align-items-start mb-2">
                    <h5 className="mb-0">{formatInr(l.principal)}</h5>
                    <StatusBadge status={l.status} />
                  </div>
                  <p className="text-muted mb-3">
                    Outstanding: <strong>{formatInr(l.outstandingPrincipal)}</strong>
                  </p>
                  <div className="d-flex gap-2 flex-wrap">
                    <Link to={`/borrower/loans/${l.id}`} className="btn btn-primary btn-sm">
                      <i className="bi bi-calendar-check me-1" />
                      Schedule & Pay
                    </Link>
                    <Link to={`/borrower/statement/${l.id}`} className="btn btn-outline-primary btn-sm">
                      <i className="bi bi-receipt me-1" />
                      Statement
                    </Link>
                  </div>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </>
  );
}
