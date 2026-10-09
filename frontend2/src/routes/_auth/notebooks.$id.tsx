import { createFileRoute, Outlet, useMatch } from '@tanstack/react-router';

import { NotebookHeader } from '@/components/notebooks/details/notebook-header';
import { useNotebook } from '@/lib/api/notebooks';
import { usePageTitle } from '@/lib/hooks/use-page-title';

export const Route = createFileRoute('/_auth/notebooks/$id')({
  component: NotebookPage,
});

function NotebookPage() {
  const { id } = Route.useParams();
  // The tabs below call this too and are served from cache; one query, two readers.
  const { data: notebook } = useNotebook(id);
  // Set here rather than in the tabs: child effects run first, so two callers would fight.
  const onExperiments = useMatch({ from: '/_auth/notebooks/$id/experiments', shouldThrow: false });
  usePageTitle(notebook && `Notebook ${notebook.name}`, onExperiments && 'Experiments');

  return (
    <>
      <NotebookHeader notebookId={id} notebook={notebook} />
      <Outlet />
    </>
  );
}
