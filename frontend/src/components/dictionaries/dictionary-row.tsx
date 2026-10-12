import { BookOpen } from 'lucide-react';

import { LabelledColumn } from '@/components/common/labelled-column';
import { cn } from '@/lib/utils';

import type { Dictionary } from '@/lib/types/dictionaries.ts';
import { formatDate } from '@/lib/utils.ts';

/**
 * The same column grid as `ProjectRow`: `auto-fit` gives equal columns when there is room and
 * drops to fewer as the row narrows, measured against the row itself.
 */
const COLUMNS_CLASS = 'grid gap-4 grid-cols-[repeat(auto-fit,minmax(160px,1fr))]';

/**
 * One dictionary in the admin list. A `<button>` rather than a `<Link>`: opening a dictionary
 * sets a search param on this same route instead of navigating to one of its own, so there is no
 * destination to link to.
 *
 * `aria-pressed` rather than `aria-current`: this is a toggle whose pressed state means "its
 * sheet is open", not a position in a set of navigation targets.
 */
export function DictionaryRow({
  dictionary,
  selected,
  onSelect,
}: {
  dictionary: Dictionary;
  selected: boolean;
  onSelect: () => void;
}) {
  return (
    <button
      type="button"
      aria-pressed={selected}
      onClick={onSelect}
      className={cn(
        'flex cursor-pointer flex-col gap-3 rounded-6 border bg-card px-4 pt-3 pb-4 text-left outline-none',
        'focus-visible:ring-3 focus-visible:ring-ring/50',
        selected ? 'border-blue-400 bg-blue-10' : 'border-neutral-300',
      )}
    >
      <header className="flex h-7 items-center gap-4">
        <BookOpen className="size-5 shrink-0 text-neutral-800" />
        <h3 className="flex-1 truncate text-[14px]/5 font-semibold">{dictionary.name}</h3>
      </header>

      <dl className={COLUMNS_CLASS}>
        <LabelledColumn label="Description">
          <span className="truncate">{dictionary.description || '—'}</span>
        </LabelledColumn>
        <LabelledColumn label="Last Edited on">
          <span className="truncate">{formatDate(dictionary.modifiedAt)}</span>
        </LabelledColumn>
        <LabelledColumn label="Last Edited by">
          <span className="truncate">{dictionary.modifiedBy.displayName}</span>
        </LabelledColumn>
      </dl>
    </button>
  );
}
