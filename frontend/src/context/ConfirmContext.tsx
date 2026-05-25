import React, { createContext, useCallback, useContext, useRef, useState } from 'react';
import { Button, Modal } from 'react-bootstrap';

type ConfirmOptions = {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  variant?: 'primary' | 'danger' | 'warning' | 'success';
};

type ConfirmContextValue = {
  confirm: (options: ConfirmOptions) => Promise<boolean>;
};

const ConfirmContext = createContext<ConfirmContextValue | null>(null);

export function ConfirmProvider({ children }: { children: React.ReactNode }) {
  const [open, setOpen] = useState(false);
  const [opts, setOpts] = useState<ConfirmOptions | null>(null);
  const resolver = useRef<((v: boolean) => void) | null>(null);

  const confirm = useCallback((options: ConfirmOptions) => {
    setOpts(options);
    setOpen(true);
    return new Promise<boolean>((resolve) => {
      resolver.current = resolve;
    });
  }, []);

  const close = (result: boolean) => {
    setOpen(false);
    resolver.current?.(result);
    resolver.current = null;
  };

  return (
    <ConfirmContext.Provider value={{ confirm }}>
      {children}
      <Modal show={open} onHide={() => close(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>{opts?.title}</Modal.Title>
        </Modal.Header>
        <Modal.Body>{opts?.message}</Modal.Body>
        <Modal.Footer>
          <Button variant="outline-secondary" onClick={() => close(false)}>
            {opts?.cancelText ?? 'Cancel'}
          </Button>
          <Button variant={opts?.variant ?? 'primary'} onClick={() => close(true)}>
            {opts?.confirmText ?? 'Confirm'}
          </Button>
        </Modal.Footer>
      </Modal>
    </ConfirmContext.Provider>
  );
}

export function useConfirm() {
  const ctx = useContext(ConfirmContext);
  if (!ctx) throw new Error('useConfirm outside ConfirmProvider');
  return ctx;
}
