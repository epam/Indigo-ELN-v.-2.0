import { cn } from '@/lib/utils';

import type { ReactionOutputType } from '@/lib/types/reactions.ts';
import { OUTPUT_TYPE_LABELS, OUTPUT_TYPE_TRIGGER_CLASS } from '@/lib/types/reactions.ts';

/**
 * The product type as a static pill: which of the three things this output is.
 *
 * Not `OutputTypeCell` with `editable={false}`, which renders a disabled `<select>` — the batch
 * summary does not edit the type (the products table owns `SetOutputRowType`), and offering a
 * control where there is no choice to make is worse than showing none. The colours are the
 * trigger's, so a batch row and a product row read the same type the same way.
 */
export function OutputTypeBadge({ value }: { value: ReactionOutputType }) {
  return (
    <span
      className={cn(
        'inline-flex cursor-default items-center rounded-md border px-2 py-0.5 text-[13px]/5 text-neutral-1000',
        OUTPUT_TYPE_TRIGGER_CLASS[value],
      )}
    >
      {OUTPUT_TYPE_LABELS[value]}
    </span>
  );
}
