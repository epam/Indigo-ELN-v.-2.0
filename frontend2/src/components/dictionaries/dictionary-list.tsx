import { DictionaryRow } from '@/components/dictionaries/dictionary-row';
import { DictionaryRowSkeleton } from '@/components/dictionaries/dictionary-row-skeleton';
import { describeError } from '@/lib/toast';

import type { UseQueryResult } from '@tanstack/react-query';
import type { Dictionary } from '@/lib/types/dictionaries.ts';

/** Roughly a screenful, so the first load lands without the document resizing under the reader. */
const FIRST_LOAD_SKELETONS = 6;

const LIST_CLASS = 'flex flex-col gap-3';

/**
 * The dictionaries, as stacked rows.
 *
 * `InfiniteLoader` would have been the home for the pending/error/empty branches, but it is typed
 * on `UseInfiniteQueryResult<InfiniteData<Page<T>>>` and `GET /dictionaries` declares no query
 * params at all — no paging, no search, ordered by name. There is no page to fetch a second of,
 * so the three branches are repeated here in the shape it and `VersionHistoryPanel` share.
 *
 * The query is passed in rather than fetched here: the route needs the same list to resolve which
 * dictionary its search param names.
 */
export function DictionaryList({
  query,
  selectedId,
  onSelect,
}: {
  query: UseQueryResult<Dictionary[], Error>;
  selectedId: string | undefined;
  onSelect: (dictionary: Dictionary) => void;
}) {
  const { data, error, isPending } = query;

  if (isPending) {
    return (
      <div aria-busy="true" className={LIST_CLASS}>
        <span className="sr-only">Loading dictionaries…</span>
        {Array.from({ length: FIRST_LOAD_SKELETONS }, (_, index) => (
          <DictionaryRowSkeleton key={index} />
        ))}
      </div>
    );
  }

  if (error) {
    // Through describeError, not `error.message` — that is ApiError's constructor string. The
    // toast apiFetch already raised says the same thing, but a toast is gone in five seconds and
    // an empty list is not.
    return <p className="text-[14px]/6 text-destructive">Could not load dictionaries: {describeError(error)[0]}</p>;
  }

  if (data.length === 0) {
    return <p className="text-[14px]/6 text-neutral-700">No dictionaries found.</p>;
  }

  return (
    <div className={LIST_CLASS}>
      {data.map((dictionary) => (
        <DictionaryRow
          key={dictionary.id}
          dictionary={dictionary}
          selected={dictionary.id === selectedId}
          onSelect={() => onSelect(dictionary)}
        />
      ))}
    </div>
  );
}
