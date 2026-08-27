import { Toast } from '@base-ui/react/toast';
import { X } from 'lucide-react';
import type * as React from 'react';

import { toastManager } from '@/lib/toast';
import { cn } from '@/lib/utils';

/**
 * Wraps the app in the toast context and renders the viewport. Bound to the module-level
 * `toastManager` so non-React code — `apiFetch` in particular — can raise toasts too.
 */
function ToastProvider({ children }: { children: React.ReactNode }) {
  return (
    <Toast.Provider toastManager={toastManager}>
      {children}
      <Toast.Portal>
        <Toast.Viewport className="fixed top-4 right-4 z-100 flex w-[380px] max-w-[calc(100vw-2rem)] flex-col">
          <ToastList />
        </Toast.Viewport>
      </Toast.Portal>
    </Toast.Provider>
  );
}

function ToastList() {
  const { toasts } = Toast.useToastManager();

  return toasts.map((toast) => (
    <Toast.Root
      key={toast.id}
      toast={toast}
      // Base UI stacks toasts absolutely and drives the offsets through these two
      // variables; without them every toast would sit on top of the last.
      className={cn(
        'absolute right-0 bottom-0 left-auto w-full rounded-6 border bg-card p-4 shadow-card',
        'translate-x-[var(--toast-swipe-movement-x)] translate-y-[calc(var(--toast-swipe-movement-y)+var(--toast-offset-y))]',
        'transition-all duration-200 data-[ending-style]:opacity-0 data-[starting-style]:opacity-0',
        'data-[ending-style]:translate-x-full data-[starting-style]:translate-x-full',
        toast.type === 'error' ? 'border-red-200 bg-red-10' : 'border-neutral-300',
      )}
    >
      {/* whitespace-pre-line: a bean-validation body arrives as newline-joined lines. */}
      <Toast.Title className="pr-6 text-[14px]/6 font-medium whitespace-pre-line text-neutral-1000" />
      <Toast.Description className="pr-6 text-[12px]/5 whitespace-pre-line text-neutral-700" />
      {/*
        Mounted unconditionally: Base UI renders it as null unless the toast was added
        with `actionProps`, so only the toasts that offer an action grow a button.
      */}
      <Toast.Action className="mt-2 cursor-pointer rounded-2 text-[12px]/5 font-semibold text-blue-400 underline underline-offset-2 outline-none hover:text-blue-600 focus-visible:ring-3 focus-visible:ring-ring/50" />
      <Toast.Close
        aria-label="Dismiss"
        className="absolute top-3 right-3 cursor-pointer rounded-2 p-0.5 text-neutral-700 hover:bg-neutral-200 hover:text-neutral-1000"
      >
        <X className="size-4" />
      </Toast.Close>
    </Toast.Root>
  ));
}

export { ToastProvider };
