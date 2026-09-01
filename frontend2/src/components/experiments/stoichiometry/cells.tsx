import { Trash2 } from 'lucide-react';
import { useState } from 'react';

import { SavingOverlay } from '@/components/common/saving-overlay';
import { ROLE_LABELS } from '@/components/experiments/stoichiometry/columns';
import { MultiCombobox } from '@/components/ui/combobox';
import { Select } from '@/components/ui/select';
import { useDictionary } from '@/lib/api/dictionaries';
import { cn } from '@/lib/utils';

import type { BuiltInDictionary, DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { ReactionRole } from '@/lib/types/reactions.ts';

/**
 * The absence marker every read-only cell shares. Centred, so a column of them reads as a column.
 *
 * `cursor-default` here and on every other read-only cell below. The initial `cursor: auto`
 * resolves to a text I-beam over text content, and an I-beam is the cursor that says "you can
 * type here" — on a cell that cannot be edited it is a standing invitation to click and find
 * nothing happens. The editable cells opt back in to `cursor-text` themselves.
 */
export function EmptyCell() {
  return <span className="block cursor-default text-center text-neutral-700">—</span>;
}

/** Read-only text. Truncates rather than wrapping — a row is one line tall. */
export function ReadonlyCell({ value }: { value: string | undefined }) {
  if (value == null || value === '') return <EmptyCell />;
  return (
    <span className="block cursor-default truncate text-[13px]/5 text-neutral-1000" title={value}>
      {value}
    </span>
  );
}

/**
 * Server-rendered HTML, which for this table means one thing: a molecular formula.
 * `MolFormula` serialises through `@JsonValue toHTMLString()`, so `C4H6O3` arrives as
 * `C<sub>4</sub>H<sub>6</sub>O<sub>3</sub>` and rendering it as text would show the tags.
 *
 * The only markup the backend emits here is `<sub>`, and the string is composed from an
 * element/count table rather than from user input, so there is nothing to sanitise.
 */
export function FormulaCell({ value }: { value: string | undefined }) {
  if (value == null || value === '') return <EmptyCell />;
  return (
    <span
      /*
        `py-1` is for the subscripts, not for spacing. `truncate` brings `overflow: hidden`, which
        clips at the padding box, and a subscript on `align-sub` sits ~2px below the line box —
        so its digits were being shaved, which reads as them vanishing at whichever zoom level
        rounding tips over. Padding is the right lever: raising `line-height` instead only chases
        its own tail, because the subscript inherits it and its box grows to match.

        `leading-none` on the subscripts keeps their inline boxes from inflating the line for the
        same reason. The line-height stays at the table's own 20px so the formula sits on the same
        baseline as every other cell, and the row is taller than this anyway, so it costs nothing.
      */
      className="block cursor-default truncate py-1 text-[13px]/5 text-neutral-1000 [&_sub]:align-sub [&_sub]:text-[0.75em] [&_sub]:leading-none"
      dangerouslySetInnerHTML={{ __html: value }}
    />
  );
}

/**
 * Free text, saved when the field is left — the same contract as every text field on this
 * screen, and the reason there is no Save button anywhere on it.
 *
 * The draft is local so the cell shows what was typed while the request is in flight, and a
 * failed save leaves it there rather than snapping back to the server's value.
 */
export function TextCell({
  value,
  editable,
  pending,
  label,
  onCommit,
}: {
  value: string | undefined;
  editable: boolean;
  pending: boolean;
  label: string;
  onCommit: (next: string | null) => void;
}) {
  const [draft, setDraft] = useState(value ?? '');

  if (!editable) return <ReadonlyCell value={value} />;

  return (
    <SavingOverlay pending={pending} spinner="center" className="w-full">
      <input
        type="text"
        aria-label={label}
        value={draft}
        placeholder="—"
        onChange={(event) => setDraft(event.target.value)}
        onBlur={() => {
          const next = draft.trim() === '' ? null : draft.trim();
          // Both sides normalised to null, so "" and undefined are not seen as a change.
          if (next !== (value ?? null)) onCommit(next);
        }}
        className={cn(
          'w-full rounded-2 border border-transparent px-2 py-1 text-[13px]/5 text-neutral-1000 outline-none',
          'placeholder:text-center placeholder:text-neutral-700 hover:border-neutral-300',
          'focus:border-blue-400 focus:placeholder:text-transparent',
        )}
      />
    </SavingOverlay>
  );
}

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
    <SavingOverlay pending={pending} spinner="center" className="w-full">
      <Select<ReactionRole>
        aria-label="Reaction role"
        size="sm"
        value={value}
        items={[...roles]}
        itemToKey={(role) => role}
        itemToLabel={(role) => ROLE_LABELS[role]}
        disabled={!editable}
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

/**
 * One item from a built-in dictionary, or none.
 *
 * A `Select` rather than a `Combobox`, for the same reason Rxn Role is one: these lists are short
 * and closed, so a text input to filter them with only invites typing a value that cannot be
 * accepted. Unlike Rxn Role the field is optional — `SetInputRowSaltCode` takes a null — so it
 * carries an `emptyLabel`, which puts clearing in the list rather than behind a ✕ that a select
 * does not have.
 */
export function DictionaryCell({
  dictionary,
  value,
  editable,
  pending,
  label,
  onCommit,
}: {
  dictionary: BuiltInDictionary;
  value: DictionaryItemRef | undefined;
  editable: boolean;
  pending: boolean;
  label: string;
  onCommit: (next: DictionaryItemRef | null) => void;
}) {
  const { data, isPending, isError } = useDictionary(dictionary);

  if (!editable) return <ReadonlyCell value={value?.name} />;

  return (
    <SavingOverlay pending={pending} spinner="center" className="w-full">
      <Select<DictionaryItemRef>
        aria-label={label}
        size="sm"
        value={value ?? null}
        items={data ?? []}
        itemToKey={(item) => item.id}
        itemToLabel={(item) => item.name}
        emptyLabel="—"
        loading={isPending}
        // apiFetch has already toasted the failure; this says why the list is empty.
        error={isError}
        onValueChange={(next) => {
          if ((next?.id ?? null) !== (value?.id ?? null)) onCommit(next);
        }}
      />
    </SavingOverlay>
  );
}

/**
 * Several items from a built-in dictionary — health hazards.
 *
 * `MultiCombobox` filters nothing itself, so the list is narrowed here against the typed
 * input. `allowCustomValues` stays off: a hazard has to be a dictionary entry.
 */
export function MultiDictionaryCell({
  dictionary,
  value,
  editable,
  pending,
  label,
  onCommit,
}: {
  dictionary: BuiltInDictionary;
  value: DictionaryItemRef[];
  editable: boolean;
  pending: boolean;
  label: string;
  onCommit: (next: DictionaryItemRef[]) => void;
}) {
  const [inputValue, setInputValue] = useState('');
  const { data, isPending, isError } = useDictionary(dictionary);

  if (!editable) return <ReadonlyCell value={value.map((item) => item.name).join(', ') || undefined} />;

  const term = inputValue.trim().toLowerCase();
  const items = (data ?? []).filter((item) => item.name.toLowerCase().includes(term));

  return (
    <SavingOverlay pending={pending} spinner="center" className="w-full">
      <MultiCombobox<DictionaryItemRef>
        aria-label={label}
        size="sm"
        value={value}
        items={items}
        itemToKey={(item) => item.id}
        itemToLabel={(item) => item.name}
        inputValue={inputValue}
        onInputValueChange={setInputValue}
        loading={isPending}
        error={isError}
        // `@NotNull List` on the record: emptying the list sends `[]`, never null.
        onValueChange={onCommit}
      />
    </SavingOverlay>
  );
}

/** The row's delete button. */
export function DeleteCell({
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
    <SavingOverlay pending={pending} spinner="center" className="mx-auto w-fit">
      <button
        type="button"
        aria-label={label}
        disabled={!editable}
        onClick={onCommit}
        className={cn(
          'rounded-2 p-1 text-red-200 outline-none',
          'hover:bg-red-10 focus-visible:ring-3 focus-visible:ring-ring/50',
          'disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-transparent',
        )}
      >
        <Trash2 className="size-4" />
      </button>
    </SavingOverlay>
  );
}
