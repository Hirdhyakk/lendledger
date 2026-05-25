import { Container, Nav, Navbar, NavDropdown } from 'react-bootstrap';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Layout() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  if (!user) return <Outlet />;

  const links = isAdmin
    ? [
        { to: '/admin/dashboard', label: 'Dashboard', icon: 'speedometer2' },
        { to: '/admin/borrowers', label: 'Borrowers', icon: 'people' },
        { to: '/admin/loans', label: 'Loans', icon: 'cash-stack' },
        { to: '/admin/reports', label: 'Reports', icon: 'bar-chart' },
      ]
    : [{ to: '/borrower/dashboard', label: 'My Loans', icon: 'wallet2' }];

  return (
    <>
      <Navbar expand="lg" className="shadow-sm" style={{ background: 'var(--ll-primary)' }} variant="dark">
        <Container fluid="lg">
          <Navbar.Brand as={Link} to={links[0].to}>
            <i className="bi bi-bank2 me-2" />
            LendLedger
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="main-nav" />
          <Navbar.Collapse id="main-nav">
            <Nav className="me-auto">
              {links.map(({ to, label, icon }) => (
                <NavLink
                  key={to}
                  to={to}
                  className={({ isActive }) =>
                    `nav-link d-flex align-items-center gap-1${isActive ? ' active fw-semibold' : ''}`
                  }
                >
                  <i className={`bi bi-${icon}`} />
                  {label}
                </NavLink>
              ))}
            </Nav>
            <Nav>
              <NavDropdown
                title={
                  <span>
                    <i className="bi bi-person-circle me-1" />
                    {user.fullName}
                  </span>
                }
                align="end"
              >
                <NavDropdown.ItemText>
                  <small className="text-muted">{user.email}</small>
                  <br />
                  <span className="badge bg-secondary">{user.role}</span>
                </NavDropdown.ItemText>
                <NavDropdown.Divider />
                <NavDropdown.Item
                  onClick={() => {
                    logout();
                    navigate('/login');
                  }}
                >
                  <i className="bi bi-box-arrow-right me-2" />
                  Logout
                </NavDropdown.Item>
              </NavDropdown>
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
      <Container fluid="lg" className="py-4">
        <Outlet />
      </Container>
      <footer className="text-center text-muted small py-4 border-top bg-white">
        <Container>
          LendLedger — educational loan management prototype · Not licensed lending
        </Container>
      </footer>
    </>
  );
}
