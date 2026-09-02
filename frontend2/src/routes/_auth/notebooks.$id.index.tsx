import { createFileRoute } from '@tanstack/react-router';

import { TeamCard } from '@/components/common/team-card';
import { AboutNotebookCard } from '@/components/notebooks/about-notebook-card';
import { NotebookInfoSkeleton } from '@/components/notebooks/notebook-info-skeleton';
import { useNotebook, useUpdateNotebookAccess } from '@/lib/api/notebooks';

export const Route = createFileRoute('/_auth/notebooks/$id/')({
  component: NotebookInfoTab,
});

/** The same 1fr / 512px split the project's Info tab uses, stacking once Team no longer fits. */
const COLUMNS_CLASS = 'grid items-start gap-4 grid-cols-1 xl:grid-cols-[minmax(0,1fr)_512px]';

function NotebookInfoTab() {
  const { id } = Route.useParams();
  const { data: notebook, isPending, isError } = useNotebook(id);
  // Two instances of one mutation — see TeamCard for why they are not shared. Called before
  // the early returns below, so the hook order never changes with the query's state.
  const addMembers = useUpdateNotebookAccess(id);
  const changeLevel = useUpdateNotebookAccess(id);

  if (isPending) return <NotebookInfoSkeleton />;
  // apiFetch has already toasted the failure; this is the page saying what it cannot show.
  if (isError || !notebook) {
    return <p className="text-[14px]/6 text-destructive">This notebook could not be loaded.</p>;
  }

  return (
    <div className={COLUMNS_CLASS}>
      <AboutNotebookCard notebook={notebook} />
      <TeamCard
        acl={notebook.acl}
        canManage={notebook.currentPermissions.includes('MANAGE_NOTEBOOK_ACCESS')}
        addMembers={addMembers}
        changeLevel={changeLevel}
      />
    </div>
  );
}
