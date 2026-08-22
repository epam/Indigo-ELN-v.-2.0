import { Dialog as DialogPrimitive } from '@base-ui/react/dialog';
import { X } from 'lucide-react';

import { cn } from '@/lib/utils';

const DialogRoot = DialogPrimitive.Root;
const DialogTrigger = DialogPrimitive.Trigger;
const DialogClose = DialogPrimitive.Close;

/**
 * The whole modal frame: backdrop, centred panel, and the title row with its close
 * button. `children` is the body; `footer` is pinned below the scroll area so long
 * content scrolls under a fixed action row, as in the design.
 */
function DialogContent({
  title,
  description,
  footer,
  className,
  children,
  ...props
}: DialogPrimitive.Popup.Props & {
  title: string;
  description?: string;
  footer?: React.ReactNode;
}) {
  return (
    <DialogPrimitive.Portal>
      <DialogPrimitive.Backdrop className="fixed inset-0 z-50 bg-neutral-1000/40 transition-opacity duration-150 data-[ending-style]:opacity-0 data-[starting-style]:opacity-0" />
      <DialogPrimitive.Popup
        className={cn(
          'fixed top-1/2 left-1/2 z-50 flex max-h-[90vh] w-[560px] max-w-[calc(100vw-2rem)] -translate-x-1/2 -translate-y-1/2 flex-col',
          'rounded-6 bg-card shadow-card outline-none transition-all duration-150',
          'data-[ending-style]:scale-95 data-[ending-style]:opacity-0 data-[starting-style]:scale-95 data-[starting-style]:opacity-0',
          className,
        )}
        {...props}
      >
        <div className="flex items-center justify-between gap-4 border-b border-neutral-300 px-6 py-4">
          <DialogPrimitive.Title className="text-[18px]/7 font-semibold text-neutral-1000">
            {title}
          </DialogPrimitive.Title>
          <DialogPrimitive.Close
            aria-label="Close"
            className="cursor-pointer rounded-2 p-1 text-neutral-700 outline-none hover:bg-neutral-200 hover:text-neutral-1000 focus-visible:ring-3 focus-visible:ring-ring/50"
          >
            <X className="size-5" />
          </DialogPrimitive.Close>
        </div>

        {description && (
          <DialogPrimitive.Description className="px-6 pt-4 text-[14px]/6 text-neutral-700">
            {description}
          </DialogPrimitive.Description>
        )}

        <div className="flex min-h-0 flex-1 flex-col gap-4 overflow-y-auto p-6">{children}</div>

        {footer && <div className="flex justify-end gap-3 border-t border-neutral-300 px-6 py-4">{footer}</div>}
      </DialogPrimitive.Popup>
    </DialogPrimitive.Portal>
  );
}

export { DialogRoot as Dialog, DialogTrigger, DialogClose, DialogContent };
