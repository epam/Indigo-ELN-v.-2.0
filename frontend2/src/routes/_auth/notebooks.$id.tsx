import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/_auth/notebooks/$id')({
  component: NotebookPage,
});

function NotebookPage() {
  const { id } = Route.useParams();
  return <h1 className="text-[16px]/6 font-semibold">Notebook {id} — coming soon</h1>;
}
