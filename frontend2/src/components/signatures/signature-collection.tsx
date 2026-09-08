import { InfiniteLoader, type InfiniteLoaderLayout } from '@/components/common/infinite-loader';
import { SignatureRow } from '@/components/signatures/signature-row';
import { SignatureRowSkeleton } from '@/components/signatures/signature-row-skeleton';
import { COLLECTION_PAGE_SIZE } from '@/lib/api/collections';
import { useSignatureDocuments } from '@/lib/api/signatures';

import type { SignatureDocument, SignatureFilters } from '@/lib/types/signatures.ts';

/**
 * One layout, not two: a signature document has no thumbnail and no counts, so the card a grid
 * view would hold is the row with its columns stacked. The bar therefore offers no view toggle.
 */
const LAYOUT: InfiniteLoaderLayout<SignatureDocument> = {
  className: 'flex flex-col gap-3',
  Item: SignatureRow,
  ItemSkeleton: SignatureRowSkeleton,
};

export function SignatureCollection({ filters }: { filters: SignatureFilters }) {
  return (
    <InfiniteLoader
      entityLabel="signatures"
      layout={LAYOUT}
      query={useSignatureDocuments(filters)}
      firstLoadSkeletons={COLLECTION_PAGE_SIZE}
    />
  );
}
