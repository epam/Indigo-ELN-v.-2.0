import { Plus } from 'lucide-react';
import { useMemo, useState } from 'react';

import { DeleteCell, TextCell } from '@/components/experiments/stoichiometry/cells';
import { CELL_CLASS, EDITABLE_CELL_CLASS, HEADER_CELL_CLASS } from '@/components/experiments/stoichiometry/columns';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { SearchInput } from '@/components/ui/search-input';
import { Skeleton } from '@/components/ui/skeleton';
import {
  useAddDictionaryItem,
  useDictionaryItems,
  useRemoveDictionaryItem,
  useUpdateDictionaryItem,
} from '@/lib/api/dictionaries';
import { describeError } from '@/lib/toast';
import { cn } from '@/lib/utils';

import type { Dictionary, DictionaryItem, DictionaryItemEditRequest } from '@/lib/types/dictionaries.ts';
import { formatDate } from '@/lib/utils.ts';

/** Rank, Name, Description, Active, Created on, delete. */
const COLUMN_COUNT = 6;

const NAME_REQUIRED = 'Name is required.';
const NAME_NOT_UNIQUE = 'Name must be unique. Please choose a different name.';

/**
 * Ported from indigo-frontend's `nameUniqueValidator`, including its case-insensitive comparison
 * and its copy. `exceptId` is the row being edited: a word is allowed to keep its own name.
 */
function validateName(name: string | null, items: DictionaryItem[], exceptId?: string): string | undefined {
  if (name === null) return NAME_REQUIRED;
  const taken = items.some((item) => item.id !== exceptId && item.name.toLowerCase() === name.toLowerCase());
  return taken ? NAME_NOT_UNIQUE : undefined;
}

/**
 * The words of one dictionary, with every column the backend lets an admin change.
 *
 * indigo-frontend's table is read-only apart from adding and deleting: its Rank and Description
 * are plain text and its Active checkbox is wired to nothing at all. `PATCH
 * /dictionaries/{ref}/{itemID}` has always accepted `name`, `description`, `ordinal` and
 * `active`, so all four are editable here.
 *
 * The whole page is behind `MANAGE_DICTIONARIES`, which is the permission every one of those
 * writes needs — so nothing here checks a permission or reads `userEditable` (which only relaxes
 * *adding* for users who could not otherwise reach this screen).
 */
export function DictionaryWordsTable({ dictionary }: { dictionary: Dictionary }) {
  const { data: items, isPending, error } = useDictionaryItems(dictionary.id);
  const [search, setSearch] = useState('');
  /** `null` when there is no draft row; the typed name while there is one. */
  const [draft, setDraft] = useState<string | null>(null);
  const [draftError, setDraftError] = useState<string | undefined>(undefined);

  const add = useAddDictionaryItem(dictionary);
  const update = useUpdateDictionaryItem(dictionary);
  const remove = useRemoveDictionaryItem(dictionary);

  // Derived at render rather than kept in a second array. indigo-frontend holds a filtered copy
  // and re-pushes the whole list on every write, which silently drops whatever was typed here.
  const all = useMemo(() => items ?? [], [items]);
  const visible = useMemo(() => {
    const term = search.trim().toLowerCase();
    // Name only, matching indigo-frontend's `applyFilter`.
    return term === '' ? all : all.filter((item) => item.name.toLowerCase().includes(term));
  }, [all, search]);

  function commitDraft() {
    const name = (draft ?? '').trim();
    // Blurring an untouched row discards it — the same gesture as Escape, and the only way out
    // of a row you opened by mistake.
    if (name === '') {
      setDraft(null);
      setDraftError(undefined);
      return;
    }
    const message = validateName(name, all);
    if (message !== undefined) {
      setDraftError(message);
      return;
    }
    // Dropped before the request goes out, not after it resolves: indigo-frontend leaves the row
    // in place until the response lands, so Enter followed by blur fires the POST twice.
    setDraft(null);
    setDraftError(undefined);
    add.mutate(name);
  }

  return (
    <section className="flex min-h-0 flex-col gap-4">
      <div className="flex items-center justify-between gap-4">
        <SearchInput aria-label="Search words" value={search} onChange={setSearch} className="max-w-80" />
        <Button
          variant="link"
          onClick={() => {
            setDraft('');
            setDraftError(undefined);
          }}
          disabled={draft !== null || isPending}
        >
          <Plus />
          Add Word
        </Button>
      </div>

      <div className="overflow-x-auto rounded-6 border border-neutral-300 bg-card">
        <table className="w-full border-collapse">
          <caption className="sr-only">Words in {dictionary.name}, in rank order</caption>
          <thead>
            <tr>
              <th scope="col" className={cn(HEADER_CELL_CLASS, 'w-20 text-left')}>
                Rank
              </th>
              <th scope="col" className={cn(HEADER_CELL_CLASS, 'text-left')}>
                Name
              </th>
              <th scope="col" className={cn(HEADER_CELL_CLASS, 'text-left')}>
                Description
              </th>
              <th scope="col" className={cn(HEADER_CELL_CLASS, 'w-20')}>
                Active
              </th>
              <th scope="col" className={cn(HEADER_CELL_CLASS, 'w-32 text-left')}>
                Created on
              </th>
              {/* The trash column speaks for itself. */}
              <th className={cn(HEADER_CELL_CLASS, 'w-11')} />
            </tr>
          </thead>

          <tbody aria-busy={isPending}>
            {isPending
              ? Array.from({ length: 5 }, (_, index) => (
                  <tr key={index}>
                    <td className={CELL_CLASS} colSpan={COLUMN_COUNT}>
                      <Skeleton className="h-5 w-full" />
                    </td>
                  </tr>
                ))
              : visible.map((item) => (
                  <WordRow
                    key={item.id}
                    item={item}
                    items={all}
                    rowCount={all.length}
                    pending={
                      (update.isPending && update.variables?.itemId === item.id) ||
                      (remove.isPending && remove.variables === item.id)
                    }
                    onEdit={(edit) => update.mutate({ itemId: item.id, edit })}
                    onDelete={() => remove.mutate(item.id)}
                  />
                ))}

            {/* Always below the list, filter or no filter: a row being typed is not a search result. */}
            {draft !== null && (
              <tr>
                <td className={CELL_CLASS} />
                <td className={CELL_CLASS}>
                  <input
                    // The row exists only because the reader just pressed Add Word; leaving focus
                    // anywhere else would make them hunt for the box they asked for.
                    autoFocus
                    type="text"
                    aria-label="New word name"
                    aria-invalid={draftError !== undefined}
                    title={draftError}
                    placeholder="Name"
                    value={draft}
                    onChange={(event) => {
                      setDraft(event.target.value);
                      setDraftError(undefined);
                    }}
                    onBlur={commitDraft}
                    onKeyDown={(event) => {
                      if (event.key === 'Enter') {
                        event.preventDefault();
                        commitDraft();
                      }
                      if (event.key === 'Escape') {
                        setDraft(null);
                        setDraftError(undefined);
                      }
                    }}
                    className={cn(
                      EDITABLE_CELL_CLASS,
                      draftError !== undefined && 'border-destructive focus:border-destructive',
                    )}
                  />
                </td>
                <td className={CELL_CLASS} colSpan={COLUMN_COUNT - 2} />
              </tr>
            )}
          </tbody>
        </table>

        {isPending && (
          // The skeleton rows are decoration; this is what a screen reader is told, and it lives
          // outside the table so it is not a row that is not a row.
          <p role="status" className="sr-only">
            Loading dictionary words…
          </p>
        )}
        {error != null && (
          <p className="p-6 text-center text-[14px]/6 text-destructive">
            Could not load dictionary words: {describeError(error)[0]}
          </p>
        )}
        {!isPending && error == null && visible.length === 0 && draft === null && (
          <p className="p-6 text-center text-[14px]/6 text-neutral-700">No dictionary words found.</p>
        )}
      </div>
    </section>
  );
}

/**
 * One saved word. Rank, Name and Description are the same on-blur text cell the stoichiometry
 * screen uses, each with the rule its column has: Rank must land inside the list, Name is
 * `@NotEmpty` and unique, Description is nullable and unconstrained.
 */
function WordRow({
  item,
  items,
  rowCount,
  pending,
  onEdit,
  onDelete,
}: {
  item: DictionaryItem;
  items: DictionaryItem[];
  rowCount: number;
  pending: boolean;
  onEdit: (edit: DictionaryItemEditRequest) => void;
  onDelete: () => void;
}) {
  return (
    <tr>
      <td className={CELL_CLASS}>
        <TextCell
          // Remounted on a renumber: one `PATCH {ordinal}` reorders the whole list, so every other
          // row's rank changes underneath a cell that holds its draft in state.
          key={item.ordinal}
          value={String(item.ordinal)}
          editable
          pending={pending}
          label={`Rank of ${item.name}`}
          validate={(next) => {
            const rank = Number(next);
            if (next === null || !Number.isInteger(rank)) return 'Rank must be a whole number.';
            return rank < 1 || rank > rowCount ? `Rank must be between 1 and ${rowCount}.` : undefined;
          }}
          onCommit={(next) => onEdit({ ordinal: Number(next) })}
        />
      </td>
      <td className={CELL_CLASS}>
        <TextCell
          value={item.name}
          editable
          pending={pending}
          label={`Name of ${item.name}`}
          validate={(next) => validateName(next, items, item.id)}
          onCommit={(next) => onEdit({ name: next! })} // validateName has rejected null already
        />
      </td>
      <td className={CELL_CLASS}>
        <TextCell
          value={item.description ?? undefined}
          editable
          pending={pending}
          label={`Description of ${item.name}`}
          onCommit={(next) => onEdit({ description: next })}
        />
      </td>
      <td className={cn(CELL_CLASS, 'text-center')}>
        <Checkbox
          aria-label={`${item.name} is active`}
          checked={item.active}
          disabled={pending}
          onCheckedChange={(active) => onEdit({ active })}
        />
      </td>
      <td className={cn(CELL_CLASS, 'text-[13px]/5 whitespace-nowrap')}>{formatDate(item.createdAt)}</td>
      <td className={CELL_CLASS}>
        <DeleteCell label={`Delete ${item.name}`} editable={!pending} pending={pending} onCommit={onDelete} />
      </td>
    </tr>
  );
}
