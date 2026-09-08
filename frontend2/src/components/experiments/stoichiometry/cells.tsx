import { Trash2 } from 'lucide-react';
import type { ComponentType } from 'react';
import { useState } from 'react';

import { SavingOverlay } from '@/components/common/saving-overlay';
import { MultiCombobox } from '@/components/ui/combobox';
import { Select } from '@/components/ui/select';
import { EDITABLE_CELL_CLASS } from '@/components/experiments/stoichiometry/columns';
import { useDictionary } from '@/lib/api/dictionaries';
import { cn } from '@/lib/utils';

import type { BuiltInDictionary, DictionaryItemRef } from '@/lib/types/dictionaries.ts';

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

/**
 * Read-only text. Truncates rather than wrapping — a row is one line tall, and the `title` is
 * what makes a truncated value readable.
 *
 * A column may override that `title` where it has something better to say than the value itself:
 * the batch summary's Reg. Status cell hangs `registrationStatusMessage` there, which is the only
 * place the reason a registration failed is shown at all.
 */
export function ReadonlyCell({ value, title }: { value: string | undefined; title?: string }) {
  if (value == null || value === '') return <EmptyCell />;
  return (
    <span className="block cursor-default truncate text-[13px]/5 text-neutral-1000" title={title ?? value}>
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
 *
 * `validate` is optional and off by default, because nothing on the stoichiometry screen has a
 * rule the server does not already accept. Where a column does — a dictionary word's name is
 * `@NotEmpty` and has to be unique — it returns the message to show, and the cell then keeps the
 * draft and issues no request at all, rather than spending a round trip to be told the same
 * thing. It is the *committed* value that is checked, so a rule never fires mid-typing.
 */
export function TextCell({
  value,
  editable,
  pending,
  label,
  validate,
  onCommit,
}: {
  value: string | undefined;
  editable: boolean;
  pending: boolean;
  label: string;
  validate?: (next: string | null) => string | undefined;
  onCommit: (next: string | null) => void;
}) {
  const [draft, setDraft] = useState(value ?? '');
  const [invalid, setInvalid] = useState<string | undefined>(undefined);

  if (!editable) return <ReadonlyCell value={value} />;

  return (
    <SavingOverlay pending={pending} spinner="center" className="w-full">
      <input
        type="text"
        aria-label={label}
        aria-invalid={invalid !== undefined}
        title={invalid}
        value={draft}
        placeholder="—"
        onChange={(event) => {
          setDraft(event.target.value);
          // Clears as soon as the value moves, so the red border is about what is in the box now.
          setInvalid(undefined);
        }}
        onBlur={() => {
          const next = draft.trim() === '' ? null : draft.trim();
          const message = validate?.(next);
          setInvalid(message);
          if (message !== undefined) return;
          // Both sides normalised to null, so "" and undefined are not seen as a change.
          if (next !== (value ?? null)) onCommit(next);
        }}
        className={cn(EDITABLE_CELL_CLASS, invalid !== undefined && 'border-destructive focus:border-destructive')}
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

/**
 * A row action as an icon button — what `AddBatchCell` and `DeleteCell` each hard-code, taken as
 * parameters so the batch summary's three actions do not become three more near-copies.
 *
 * `title` as well as `aria-label` because a **disabled** action still has to explain itself:
 * "Already synced with Products", "Sample already registered". The button stays mounted and
 * faded rather than being hidden, matching indigo-frontend — an action that vanishes leaves the
 * user wondering whether it was ever there.
 */
export function IconActionCell({
  icon: Icon,
  tone,
  label,
  editable,
  pending,
  onCommit,
}: {
  icon: ComponentType<{ className?: string }>;
  tone: 'blue' | 'green' | 'red';
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
        title={label}
        disabled={!editable}
        onClick={onCommit}
        className={cn(
          'rounded-2 p-1 outline-none focus-visible:ring-3 focus-visible:ring-ring/50',
          'disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-transparent',
          tone === 'blue' && 'text-blue-400 hover:bg-blue-10',
          tone === 'green' && 'text-green-200 hover:bg-green-10',
          tone === 'red' && 'text-red-200 hover:bg-red-10',
        )}
      >
        <Icon className="size-4" />
      </button>
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
