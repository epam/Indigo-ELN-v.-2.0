import { createFileRoute } from '@tanstack/react-router';

import { ActionBar } from '@/components/common/action-bar';
import { NotebookCollection } from '@/components/notebooks/list/notebook-collection';
import { z } from '@/lib/zod';
import { COLLECTION_VIEWS, type CollectionView, SORT_ORDERS, type SortOrder } from '@/lib/types/common.ts';

const searchSchema = z.object({
  q: z.string().optional(),
  sort: z.enum(SORT_ORDERS).default('LATEST'),
  createdByMe: z.boolean().default(false),
  view: z.enum(COLLECTION_VIEWS).default('grid'),
});

export const Route = createFileRoute('/_auth/projects_/$id/notebooks')({
  validateSearch: searchSchema,
  component: NotebooksTab,
});

function NotebooksTab() {
  const { id } = Route.useParams();
  const { q, sort, createdByMe, view } = Route.useSearch();
  const navigate = Route.useNavigate();

  // Same contract as the projects list: replace so keystrokes are not history entries, and
  // rewind the scroll on a filter change but not on the view toggle, which shows the same rows.
  const patch = (next: Partial<z.infer<typeof searchSchema>>, resetScroll = true) =>
    void navigate({ search: (prev) => ({ ...prev, ...next }), replace: true, resetScroll });

  return (
    <>
      <ActionBar
        entityLabel="notebooks"
        search={q ?? ''}
        sort={sort}
        createdByMe={createdByMe}
        view={view}
        onSearchChange={(value) => patch({ q: value || undefined })}
        onSortChange={(value: SortOrder) => patch({ sort: value })}
        onCreatedByMeChange={(value) => patch({ createdByMe: value })}
        onViewChange={(value: CollectionView) => patch({ view: value }, false)}
      />
      <NotebookCollection projectId={id} filters={{ search: q ?? '', sort, createdByMe }} view={view} />
    </>
  );
}
