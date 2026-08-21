import { createFileRoute } from '@tanstack/react-router';
import { z } from 'zod';

import { ActionBar, type ProjectView } from '@/components/projects/action-bar';
import { ProjectCollection } from '@/components/projects/project-collection';
import { StatsBar } from '@/components/projects/stats-bar';
import type { SortOrder } from '@/lib/types/projects.ts';

const searchSchema = z.object({
  q: z.string().optional(),
  sort: z.enum(['EARLIEST', 'LATEST']).default('LATEST'),
  createdByMe: z.boolean().default(false),
  view: z.enum(['grid', 'list']).default('grid'),
});

export const Route = createFileRoute('/_auth/projects')({
  validateSearch: searchSchema,
  head: () => ({ meta: [{ title: 'Indigo ELN - Projects' }] }),
  component: ProjectsPage,
});

function ProjectsPage() {
  const { q, sort, createdByMe, view } = Route.useSearch();
  const navigate = Route.useNavigate();

  // replace: keystrokes and toggles should not each become a history entry.
  // resetScroll: a filter change is a new result set, so rewind to the top; the view
  // toggle shows the same projects, so it keeps the reader's place.
  const patch = (next: Partial<z.infer<typeof searchSchema>>, resetScroll = true) =>
    void navigate({ search: (prev) => ({ ...prev, ...next }), replace: true, resetScroll });

  return (
    <>
      <StatsBar />
      <ActionBar
        search={q ?? ''}
        sort={sort}
        createdByMe={createdByMe}
        view={view}
        onSearchChange={(value) => patch({ q: value || undefined })}
        onSortChange={(value: SortOrder) => patch({ sort: value })}
        onCreatedByMeChange={(value) => patch({ createdByMe: value })}
        onViewChange={(value: ProjectView) => patch({ view: value }, false)}
      />
      <ProjectCollection filters={{ search: q ?? '', sort, createdByMe }} view={view} />
    </>
  );
}
