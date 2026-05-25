import { FormEvent, useState } from 'react';
import { Alert, Button, Card, Col, Container, Form, Row } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { api } from '../api/client';
import { getErrorMessage } from '../lib/errors';
import { notify } from '../lib/toast';

export default function LoginPage() {
  const { login, user } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('admin@lendledger.local');
  const [password, setPassword] = useState('password');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (user) {
    navigate(user.role === 'ADMIN' ? '/admin/dashboard' : '/borrower/dashboard', { replace: true });
  }

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await login(email, password);
      const me = await api.get('/auth/me');
      notify.success(`Welcome back, ${me.data.fullName}!`);
      navigate(me.data.role === 'ADMIN' ? '/admin/dashboard' : '/borrower/dashboard');
    } catch (err) {
      const msg = getErrorMessage(err, 'Invalid credentials or API unavailable');
      setError(msg);
      notify.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page d-flex align-items-center py-5">
      <Container>
        <Row className="justify-content-center">
          <Col md={5} lg={4}>
            <Card className="shadow-lg border-0 mb-3">
              <Card.Body className="p-4">
                <div className="text-center mb-4">
                  <i className="bi bi-bank2 text-primary display-4" />
                  <h1 className="h4 fw-bold mt-2 mb-0">LendLedger</h1>
                  <p className="text-muted small mb-0">Loan management platform</p>
                </div>
                {error && <Alert variant="danger">{error}</Alert>}
                <Form onSubmit={onSubmit}>
                  <Form.Group className="mb-3">
                    <Form.Label>Email</Form.Label>
                    <Form.Control type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
                  </Form.Group>
                  <Form.Group className="mb-4">
                    <Form.Label>Password</Form.Label>
                    <Form.Control type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                  </Form.Group>
                  <Button type="submit" variant="primary" className="w-100" disabled={loading}>
                    {loading ? (
                      <>
                        <span className="spinner-border spinner-border-sm me-2" />
                        Signing in…
                      </>
                    ) : (
                      <>
                        <i className="bi bi-box-arrow-in-right me-2" />
                        Sign in
                      </>
                    )}
                  </Button>
                </Form>
              </Card.Body>
            </Card>
            <Card className="border-0 shadow-sm">
              <Card.Body className="small text-muted">
                <p className="fw-semibold text-dark mb-2">Demo accounts</p>
                <p className="mb-1">
                  <strong>Admin:</strong> admin@lendledger.local / password
                </p>
                <p className="mb-0">
                  <strong>Borrower:</strong> borrower1@lendledger.local / password
                </p>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </div>
  );
}
