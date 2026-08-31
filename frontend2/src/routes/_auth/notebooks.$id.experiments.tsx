import {createFileRoute} from '@tanstack/react-router';

import {ActionBar} from '@/components/common/action-bar';
import {ExperimentCollection} from '@/components/experiments/experiment-collection';
import {StatusFilterMenu} from '@/components/experiments/status-filter-menu';
import {z} from '@/lib/zod';
import {COLLECTION_VIEWS, type CollectionView, SORT_ORDERS, type SortOrder} from '@/lib/types/common.ts';
import {EXPERIMENT_STATUSES} from '@/lib/types/experiments.ts';

const searchSchema = z.object({
  q: z.string().optional(),
  sort: z.enum(SORT_ORDERS).default('LATEST'),
  createdByMe: z.boolean().default(false),
  view: z.enum(COLLECTION_VIEWS).default('grid'),
  // The one filter the other collections have no equivalent of: `/notebooks/{id}/experiments`
  // is the only endpoint declaring a repeatable `status`, and an empty list means no filter.
  status: z.array(z.enum(EXPERIMENT_STATUSES)).default([]),
});

export const Route = createFileRoute('/_auth/notebooks/$id/experiments')({
  validateSearch: searchSchema,
  component: ExperimentsTab,
});

function ExperimentsTab() {
  const { id } = Route.useParams();
  const { q, sort, createdByMe, view, status } = Route.useSearch();
  const navigate = Route.useNavigate();

  // Same contract as the notebooks list: replace so keystrokes are not history entries, and
  // rewind the scroll on a filter change but not on the view toggle, which shows the same rows.
  const patch = (next: Partial<z.infer<typeof searchSchema>>, resetScroll = true) =>
    void navigate({ search: (prev) => ({ ...prev, ...next }), replace: true, resetScroll });

  return (
    <>
      <ActionBar
        entityLabel="experiments"
        search={q ?? ''}
        sort={sort}
        createdByMe={createdByMe}
        view={view}
        onSearchChange={(value) => patch({ q: value || undefined })}
        onSortChange={(value: SortOrder) => patch({ sort: value })}
        onCreatedByMeChange={(value) => patch({ createdByMe: value })}
        onViewChange={(value: CollectionView) => patch({ view: value }, false)}
      >
        <StatusFilterMenu value={status} onValueChange={(value) => patch({ status: value })} />
      </ActionBar>
      <ExperimentCollection
        notebookId={id}
        filters={{ search: q ?? '', sort, createdByMe, statuses: status }}
        view={view}
      />
    </>
  );
}
