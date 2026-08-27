import { InfiniteLoader, type InfiniteLoaderLayout } from '@/components/common/infinite-loader';
import { SearchResultRow, SelectResultContext } from '@/components/search/search-result-row';
import { SearchResultSkeleton } from '@/components/search/search-result-skeleton';

import type { CollectionView, Page } from '@/lib/types/common.ts';
import type { GlobalSearchResult } from '@/lib/types/search.ts';
import type { InfiniteData, UseInfiniteQueryResult } from '@tanstack/react-query';

/**
 * Deliberately fewer than a page: the list screens size this to their page size so a full
 * first page lands without the document resizing, but a page here is 20 tall cards inside a
 * sheet, and a screen of placeholders would overstate what is coming.
 */
const FIRST_LOAD_SKELETONS = 4;

const LAYOUT: InfiniteLoaderLayout<GlobalSearchResult> = {
  className: 'flex flex-col gap-3',
  Item: SearchResultRow,
  ItemSkeleton: SearchResultSkeleton,
};

// Results have one layout; InfiniteLoader is keyed by CollectionView, so both keys point at it.
const LAYOUTS: Record<CollectionView, InfiniteLoaderLayout<GlobalSearchResult>> = { list: LAYOUT, grid: LAYOUT };

/**
 * The results half of the search sheet: a count, then the hits.
 *
 * `InfiniteLoader` already owns the first-load skeletons, the error and empty branches, the
 * skeleton tail while the next page loads, and the IntersectionObserver sentinel — which
 * needs no adjusting in here, since the dialog body it scrolls in clips it just as the
 * page does.
 *
 * The count comes straight off the first page: the backend computes it with a window
 * function over the whole match set, so it is exact rather than a running total.
 */
function SearchResults({
  query,
  onSelect,
}: {
  query: UseInfiniteQueryResult<InfiniteData<Page<GlobalSearchResult>>, Error>;
  /** Called when a result is clicked, so the sheet can close before the route changes. */
  onSelect: () => void;
}) {
  const totalItems = query.data?.pages[0]?.totalItems;

  return (
    <section className="flex flex-col gap-3">
      {/* Suppressed at zero: InfiniteLoader's own empty branch already says there was nothing. */}
      {totalItems !== undefined && totalItems > 0 && (
        <h2 className="text-[16px]/6 font-semibold">Search Results ({totalItems})</h2>
      )}
      <SelectResultContext value={onSelect}>
        <InfiniteLoader<GlobalSearchResult>
          entityLabel="results"
          view="list"
          layouts={LAYOUTS}
          query={query}
          firstLoadSkeletons={FIRST_LOAD_SKELETONS}
        />
      </SelectResultContext>
    </section>
  );
}

export { SearchResults };
