import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import App from './App';
import { AuthProvider } from './auth/AuthContext';
import { ConfirmProvider } from './context/ConfirmContext';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    mutations: { retry: 0 },
    queries: { retry: 1, refetchOnWindowFocus: false },
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ConfirmProvider>
          <App />
          <Toaster
            position="top-right"
            toastOptions={{
              className: 'shadow',
              style: { fontSize: '0.9rem' },
              success: { iconTheme: { primary: '#198754', secondary: '#fff' } },
              error: { duration: 5000 },
            }}
          />
        </ConfirmProvider>
      </AuthProvider>
    </QueryClientProvider>
  </React.StrictMode>,
);
