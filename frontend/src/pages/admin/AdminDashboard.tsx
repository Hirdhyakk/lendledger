import { useQuery } from '@tanstack/react-query';
import { Alert, Card, Col, Row } from 'react-bootstrap';
import { api, formatInr } from '../../api/client';
import AdminWorkflow from '../../components/AdminWorkflow';
import LoadingBlock from '../../components/LoadingBlock';
import PageHeader from '../../components/PageHeader';

export default function AdminDashboard() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin-stats'],
    queryFn: () => api.get('/admin/dashboard/stats').then((r) => r.data),
  });

  return (
    <>
      <PageHeader
        title="Dashboard"
        summary="Overview of your lending portfolio. Metrics update when loans are disbursed and EMIs are paid."
      />
      <AdminWorkflow />
      {isError && <Alert variant="danger">Could not load dashboard stats. Is loan-service running?</Alert>}
      {isLoading ? (
        <LoadingBlock />
      ) : (
        <Row className="g-3">
          <Col md={4}>
            <Card className="stat-card h-100">
              <Card.Body>
                <div className="d-flex justify-content-between align-items-start">
                  <div>
                    <p className="text-muted small mb-1">Active loans</p>
                    <h2 className="mb-0 fw-bold">{data?.activeLoans ?? 0}</h2>
                  </div>
                  <i className="bi bi-cash-stack fs-2 text-primary opacity-50" />
                </div>
                <p className="small text-muted mt-2 mb-0">Disbursed and not closed</p>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="stat-card h-100" style={{ borderLeftColor: '#dc3545' }}>
              <Card.Body>
                <div className="d-flex justify-content-between align-items-start">
                  <div>
                    <p className="text-muted small mb-1">Overdue EMIs</p>
                    <h2 className="mb-0 fw-bold">{data?.overdueEmis ?? 0}</h2>
                  </div>
                  <i className="bi bi-exclamation-triangle fs-2 text-danger opacity-50" />
                </div>
                <p className="small text-muted mt-2 mb-0">Past due, not fully paid</p>
              </Card.Body>
            </Card>
          </Col>
          <Col md={4}>
            <Card className="stat-card h-100" style={{ borderLeftColor: '#198754' }}>
              <Card.Body>
                <div className="d-flex justify-content-between align-items-start">
                  <div>
                    <p className="text-muted small mb-1">Total outstanding</p>
                    <h2 className="mb-0 fw-bold">{formatInr(data?.totalOutstanding ?? 0)}</h2>
                  </div>
                  <i className="bi bi-graph-down fs-2 text-success opacity-50" />
                </div>
                <p className="small text-muted mt-2 mb-0">Principal owed on active loans</p>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      )}
    </>
  );
}
