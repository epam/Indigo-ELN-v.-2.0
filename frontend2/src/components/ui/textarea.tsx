import { INPUT_BOX, INPUT_BOX_FOCUS } from '@/components/ui/input';
import { cn } from '@/lib/utils';

import type { ComponentProps } from 'react';

/** `Input`'s multi-line sibling: the same box, sized by `rows` instead of a fixed height. */
function Textarea({ className, ...props }: ComponentProps<'textarea'>) {
  return (
    <textarea
      data-slot="textarea"
      className={cn(
        INPUT_BOX,
        INPUT_BOX_FOCUS,
        'px-3 py-2 text-[14px]/6 text-neutral-1000 outline-none',
        'placeholder:text-neutral-700 read-only:bg-neutral-200',
        'disabled:cursor-not-allowed disabled:opacity-50',
        className,
      )}
      {...props}
    />
  );
}

export { Textarea };
