import { Dialog as DialogPrimitive } from '@base-ui/react/dialog';
import { cva } from 'class-variance-authority';
import { X } from 'lucide-react';
import type * as React from 'react';

import { cn } from '@/lib/utils';

const DialogRoot = DialogPrimitive.Root;
const DialogTrigger = DialogPrimitive.Trigger;
const DialogClose = DialogPrimitive.Close;

const backdropVariants = cva(
  'fixed inset-0 z-50 transition-opacity duration-150 data-[ending-style]:opacity-0 data-[starting-style]:opacity-0',
  {
    variants: {
      // The side sheet leaves the page it searches over undimmed, as in the design;
      // Base UI still traps focus and closes on Escape or a click outside.
      side: { center: 'bg-neutral-1000/40', right: 'bg-transparent' },
    },
    defaultVariants: { side: 'center' },
  },
);

// The shadow belongs to the variant rather than to the base: the two panels sit on very
// different grounds. A centred modal is read against a dimmed page and needs no more than the
// card shadow, while a sheet leaves the page undimmed and its shadow is the only thing marking
// where the panel ends — see `--shadow-sheet` in `styles.css`.
const popupVariants = cva('fixed z-50 flex flex-col bg-card outline-none transition-all duration-150', {
  variants: {
    side: {
      center: [
        'top-1/2 left-1/2 max-h-[90vh] w-[560px] max-w-[calc(100vw-2rem)] -translate-x-1/2 -translate-y-1/2 rounded-6',
        'shadow-card',
        'data-[ending-style]:scale-95 data-[ending-style]:opacity-0 data-[starting-style]:scale-95 data-[starting-style]:opacity-0',
      ],
      // Full height, over the AppHeader rather than below it. It used to start at the header's
      // 72px, which left a strip of page above the panel and read as the sheet having slipped
      // down; the panel is `fixed z-50` and the header is an ordinary flow child, so nothing is
      // needed beyond the offset to paint over it.
      right: [
        'inset-y-0 right-0 w-[720px] max-w-full rounded-l-6',
        'shadow-sheet',
        'data-[ending-style]:translate-x-full data-[starting-style]:translate-x-full',
      ],
    },
  },
  defaultVariants: { side: 'center' },
});

/**
 * The whole modal frame: backdrop, panel, and the title row with its close button.
 * `children` is the body; `footer` is pinned below the scroll area so long content
 * scrolls under a fixed action row, as in the design. `side="right"` turns the same
 * frame into a full-height sheet anchored to the right of the window.
 */
function DialogContent({
  title,
  description,
  footer,
  side,
  className,
  children,
  ...props
}: DialogPrimitive.Popup.Props & {
  title: string;
  description?: string;
  footer?: React.ReactNode;
  side?: 'center' | 'right';
}) {
  return (
    <DialogPrimitive.Portal>
      <DialogPrimitive.Backdrop className={backdropVariants({ side })} />
      <DialogPrimitive.Popup className={cn(popupVariants({ side }), className)} {...props}>
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
