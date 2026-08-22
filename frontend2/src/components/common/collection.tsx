import type { ComponentType } from 'react';
import { useEffect, useRef } from 'react';

import type { InfiniteData, UseInfiniteQueryResult } from '@tanstack/react-query';

import type { CollectionView, Page } from '@/lib/types/common.ts';

const NEXT_PAGE_SKELETONS = 3;

/** One view's container plus the two components that fill it. */
export interface CollectionLayout<T> {
  className: string;
  Item: ComponentType<{ item: T }>;
  ItemSkeleton: ComponentType;
}

interface CollectionProps<T extends { id: string }> {
  /** Plural lower-case entity name, e.g. "projects" — fills the loading/error/empty copy. */
  entityLabel: string;
  view: CollectionView;
  layouts: Record<CollectionView, CollectionLayout<T>>;
  /** Passed in rather than fetched here, so this never learns which endpoint it renders. */
  query: UseInfiniteQueryResult<InfiniteData<Page<T>>, Error>;
  /** A full page, so a full first page lands without resizing the document. */
  firstLoadSkeletons: number;
}

/**
 * An infinitely scrolling list of entities in either layout. Everything entity-specific
 * arrives through `layouts` and `query`; the scroll sentinel, the skeleton runs and the
 * pending/error/empty branches are the same for projects, notebooks and experiments.
 */
export function Collection<T extends { id: string }>({
  entityLabel,
  view,
  layouts,
  query,
  firstLoadSkeletons,
}: CollectionProps<T>) {
  const { className, Item, ItemSkeleton } = layouts[view];
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
  if (error) {
    return (
      <p className="text-[14px]/6 text-destructive">
        Could not load {entityLabel}: {error.message}
      </p>
    );
  }

  const items = data.pages.flatMap((page) => page.items);
  if (items.length === 0) {
    return <p className="text-[14px]/6 text-neutral-700">No {entityLabel} match these filters.</p>;
  }

  return (
    <>
      <div aria-busy={isFetchingNextPage} className={className}>
        {items.map((item) => (
          <Item key={item.id} item={item} />
        ))}
        {isFetchingNextPage &&
          Array.from({ length: NEXT_PAGE_SKELETONS }, (_, index) => <ItemSkeleton key={`skeleton-${index}`} />)}
      </div>
      <div ref={sentinelRef} aria-hidden className="h-px" />
    </>
  );
}
