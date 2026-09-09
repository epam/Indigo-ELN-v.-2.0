import { SavingOverlay } from '@/components/common/saving-overlay';
import { Select } from '@/components/ui/select';

import type { ReactionRole } from '@/lib/types/reactions.ts';
import { REACTION_ROLE_LABELS } from '@/lib/types/reactions.ts';

/**
 * The reaction-role picker: four values from a fixed enum, and `@NotNull` on the record.
 *
 * A `Select`, not a `Combobox`. The combobox's three affordances are all wrong here — its text
 * input invites typing into a field that only accepts four exact values, its ✕ offers to clear
 * one the backend will reject as absent, and its "no matches" state answers a question that
 * cannot be asked. Four options need no filtering.
 */
export function RoleCell({
  value,
  roles,
  editable,
  pending,
  onCommit,
}: {
  value: ReactionRole;
  roles: readonly ReactionRole[];
  editable: boolean;
  pending: boolean;
  onCommit: (next: ReactionRole) => void;
}) {
  return (
    <SavingOverlay pending={pending} spinner="center" className="mx-auto w-fit">
      <Select<ReactionRole>
        aria-label="Reaction role"
        size="sm"
        value={value}
        items={[...roles]}
        itemToKey={(role) => role}
        itemToLabel={(role) => REACTION_ROLE_LABELS[role]}
        disabled={!editable}
        // Hugs its label instead of filling the cell, so the column can centre it — a `w-full`
        // trigger pins its label to the left however the column is aligned. The shared minimum is
        // what keeps a column of them one width rather than a ragged stack; `OutputTypeCell`
        // picked the same number for the same reason.
        className="w-auto min-w-[112px]"
        // No `emptyLabel`, so the list offers no way to reach null — but the prop allows one.
        onValueChange={(next) => next != null && next !== value && onCommit(next)}
      />
    </SavingOverlay>
  );
}

/**
 * The limiting-reagent radio. Every radio in the column shares one `name`, so the browser
 * enforces single selection across the table for free.
 *
 * There is no way to *unset* it, which matches the mutation: `SetInputRowLimiting` names the
 * row that becomes limiting and carries no boolean. A reaction either has a limiting reagent
 * or has not yet been given one.
 */
export function LimitingCell({
  checked,
  editable,
  pending,
  label,
  onCommit,
}: {
  checked: boolean;
  editable: boolean;
  pending: boolean;
  label: string;
  onCommit: () => void;
}) {
  return (
    <SavingOverlay pending={pending} spinner="center" className="mx-auto w-fit">
      <input
        type="radio"
        name="stoichiometry-limiting"
        aria-label={label}
        checked={checked}
        disabled={!editable}
        onChange={() => onCommit()}
        className="size-4 accent-blue-400"
      />
    </SavingOverlay>
  );
}
