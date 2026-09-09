import type { ComponentType } from 'react';
import { useEffect, useRef } from 'react';

import type { InfiniteData, UseInfiniteQueryResult } from '@tanstack/react-query';

import { describeError } from '@/lib/toast';

import type { Page } from '@/lib/types/common.ts';

const NEXT_PAGE_SKELETONS = 3;

/** One view's container plus the two components that fill it. */
export interface InfiniteLoaderLayout<T> {
  className: string;
  Item: ComponentType<{ item: T }>;
  ItemSkeleton: ComponentType;
}

interface InfiniteLoaderProps<T extends { id: string }> {
  /** Plural lower-case entity name, e.g. "projects" — fills the loading/error/empty copy. */
  entityLabel: string;
  /**
   * The one layout to draw. Resolved by the caller — each collection keeps its own `LAYOUTS`
   * table and indexes it by the view, so a list that has only one layout (signatures) does not
   * have to claim two, and nothing here knows what a `CollectionView` is.
   */
  layout: InfiniteLoaderLayout<T>;
  /** Passed in rather than fetched here, so this never learns which endpoint it renders. */
  query: UseInfiniteQueryResult<InfiniteData<Page<T>>, Error>;
  /** A full page, so a full first page lands without resizing the document. */
  firstLoadSkeletons: number;
}

/**
 * An infinitely scrolling list of entities. Everything entity-specific arrives through `layout`
 * and `query`; the scroll sentinel, the skeleton runs and the pending/error/empty branches are
 * the same for projects, notebooks, experiments and signatures.
 */
export function InfiniteLoader<T extends { id: string }>({
  entityLabel,
  layout,
  query,
  firstLoadSkeletons,
}: InfiniteLoaderProps<T>) {
  const { className, Item, ItemSkeleton } = layout;
  const { data, error, isPending, hasNextPage, isFetchingNextPage, fetchNextPage } = query;

  const sentinelRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel || !hasNextPage) return;

    const observer = new IntersectionObserver((entries) => {
      if (entries[0]?.isIntersecting && !isFetchingNextPage) {
        void fetchNextPage();
      }
    });
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  if (isPending) {
    return (
      <div aria-busy="true" className={className}>
        <span className="sr-only">Loading {entityLabel}…</span>
        {Array.from({ length: firstLoadSkeletons }, (_, index) => (
          <ItemSkeleton key={index} />
        ))}
      </div>
    );
  }
  // Through describeError, not `error.message`: that is ApiError's constructor string
  // ("Request failed with status 500"), which is a fact about the transport rather than
  // anything a user can act on. The same call is what worded the toast apiFetch already
  // raised — this repeats it because a toast is gone in five seconds and a failed list is not.
  const [failure] = error ? describeError(error) : [];

  /**
   * **A failure does not discard what has already loaded.** query-core sets `status: 'error'`
   * on *any* fetch failure while leaving `data` in place, so a failed next page — or a failed
   * background refetch, which `refetchOnWindowFocus` makes the common case — used to replace a
   * screenful of rows with one line of error text. `data` is the test, not `error`: with nothing
   * loaded the message is the whole answer, and with pages on screen it is a banner above them.
   */
  if (!data) {
    return (
      <p className="text-[14px]/6 text-destructive">
        Could not load {entityLabel}: {failure}
      </p>
    );
  }

  const items = data.pages.flatMap((page) => page.items);

  return (
    <>
      {/* `role="alert"`, unlike the branch above: this one appears over content the reader is
          already looking at, so nothing else would announce it. */}
      {failure && (
        <p role="alert" className="text-[14px]/6 text-destructive">
          Could not load more {entityLabel}: {failure}
        </p>
      )}

      {items.length === 0 ? (
        // Only when the list is genuinely empty. A failure that left it empty has said so above.
        !failure && <p className="text-[14px]/6 text-neutral-700">No {entityLabel} found.</p>
      ) : (
        <div aria-busy={isFetchingNextPage} className={className}>
          {items.map((item) => (
            <Item key={item.id} item={item} />
          ))}
          {isFetchingNextPage &&
            Array.from({ length: NEXT_PAGE_SKELETONS }, (_, index) => <ItemSkeleton key={`skeleton-${index}`} />)}
        </div>
      )}
      <div ref={sentinelRef} aria-hidden className="h-px" />
    </>
  );
}
