import { createFileRoute, Outlet } from '@tanstack/react-router';

import { NotebookHeader } from '@/components/notebooks/details/notebook-header';
import { useNotebook } from '@/lib/api/notebooks';

export const Route = createFileRoute('/_auth/notebooks/$id')({
  component: NotebookPage,
});

function NotebookPage() {
  const { id } = Route.useParams();
  // The tabs below call this too and are served from cache; one query, two readers.
  const { data: notebook } = useNotebook(id);

  return (
    <>
      <NotebookHeader notebookId={id} notebook={notebook} />
      <Outlet />
    </>
  );
}
