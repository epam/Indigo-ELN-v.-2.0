import { createFileRoute } from '@tanstack/react-router';

export const Route = createFileRoute('/_auth/experiments/$id')({
  component: ExperimentPage,
});

function ExperimentPage() {
  const { id } = Route.useParams();
  return <h1 className="text-[16px]/6 font-semibold">Experiment {id} — coming soon</h1>;
}
