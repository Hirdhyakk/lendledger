import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth/AuthContext';
import LoadingBlock from './components/LoadingBlock';
import LoginPage from './pages/LoginPage';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminBorrowers from './pages/admin/AdminBorrowers';
import AdminLoans from './pages/admin/AdminLoans';
import AdminLoanDetail from './pages/admin/AdminLoanDetail';
import AdminReports from './pages/admin/AdminReports';
import BorrowerDashboard from './pages/borrower/BorrowerDashboard';
import BorrowerLoanDetail from './pages/borrower/BorrowerLoanDetail';
import BorrowerStatement from './pages/borrower/BorrowerStatement';
import Layout from './components/Layout';

function PrivateRoute({ children, adminOnly = false }: { children: React.ReactNode; adminOnly?: boolean }) {
  const { user, loading } = useAuth();
  if (loading) return <LoadingBlock message="Checking session…" />;
  if (!user) return <Navigate to="/login" replace />;
  if (adminOnly && user.role !== 'ADMIN') return <Navigate to="/borrower/dashboard" replace />;
  return <>{children}</>;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<Layout />}>
          <Route path="/admin/dashboard" element={<PrivateRoute adminOnly><AdminDashboard /></PrivateRoute>} />
          <Route path="/admin/borrowers" element={<PrivateRoute adminOnly><AdminBorrowers /></PrivateRoute>} />
          <Route path="/admin/loans" element={<PrivateRoute adminOnly><AdminLoans /></PrivateRoute>} />
          <Route path="/admin/loans/:id" element={<PrivateRoute adminOnly><AdminLoanDetail /></PrivateRoute>} />
          <Route path="/admin/reports" element={<PrivateRoute adminOnly><AdminReports /></PrivateRoute>} />
          <Route path="/borrower/dashboard" element={<PrivateRoute><BorrowerDashboard /></PrivateRoute>} />
          <Route path="/borrower/loans/:id" element={<PrivateRoute><BorrowerLoanDetail /></PrivateRoute>} />
          <Route path="/borrower/statement/:id" element={<PrivateRoute><BorrowerStatement /></PrivateRoute>} />
        </Route>
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
