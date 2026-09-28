import { Plus } from 'lucide-react';

import { SavingOverlay } from '@/components/common/saving-overlay';
import { Select } from '@/components/ui/select';
import { cn } from '@/lib/utils';

import type { ReactionOutputType } from '@/lib/types/reactions.ts';
import { OUTPUT_TYPE_LABELS, OUTPUT_TYPE_TRIGGER_CLASS } from '@/lib/types/reactions.ts';

/**
 * The product-type picker: which of the three things this output is — the wanted product, a
 * by-product, or an intermediate the next step consumes.
 *
 * A `Select` for the same reasons `RoleCell` is one: three fixed values, `@NotNull` on the
 * record, nothing to filter. What it adds is colour, because the type is the row's headline and
 * a column of identical grey triggers does not read as one. The colour lives on the trigger
 * rather than in a `StatusBadge`, whose variants are the nine `ExperimentStatus` values and which
 * carries a status dot and a fixed 59px width — none of which belongs on a control.
 */
export function OutputTypeCell({
  value,
  types,
  editable,
  pending,
  label,
  onCommit,
}: {
  value: ReactionOutputType;
  types: readonly ReactionOutputType[];
  editable: boolean;
  pending: boolean;
  label: string;
  onCommit: (next: ReactionOutputType) => void;
}) {
  return (
    <SavingOverlay pending={pending} spinner="center" className="mx-auto w-fit">
      <Select<ReactionOutputType>
        aria-label={label}
        size="sm"
        value={value}
        items={[...types]}
        itemToKey={(type) => type}
        itemToLabel={(type) => OUTPUT_TYPE_LABELS[type]}
        disabled={!editable}
        // `h-7` rather than the default `h-10`: this sits in a table row beside 20px-tall text.
        className={cn('h-7 w-auto min-w-[112px] rounded-md pl-2', OUTPUT_TYPE_TRIGGER_CLASS[value])}
        // No `emptyLabel` — `@NotNull`, so the list offers no way to reach null.
        onValueChange={(next) => next != null && next !== value && onCommit(next)}
      />
    </SavingOverlay>
  );
}

/**
 * Adds a batch to this product.
 *
 * The mirror image of `DeleteCell`, and the only row-level action a product has: there is no
 * `RemoveOutputRow` mutation, because a product row is created and destroyed by editing the
 * reaction scheme rather than from this table.
 */
export function AddBatchCell({
  label,
  editable,
  pending,
  onCommit,
}: {
  label: string;
  editable: boolean;
  pending: boolean;
  onCommit: () => void;
}) {
  return (
    <SavingOverlay pending={pending} spinner="center">
      <button
        type="button"
        aria-label={label}
        disabled={!editable}
        onClick={onCommit}
        className={cn(
          'rounded-2 p-1.5 text-blue-400 outline-none',
          'hover:bg-blue-10 focus-visible:ring-3 focus-visible:ring-ring/50',
          'disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-transparent',
        )}
      >
        <Plus className="size-4" />
      </button>
    </SavingOverlay>
  );
}
