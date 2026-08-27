import { createFileRoute } from '@tanstack/react-router';

import { prewarmKetcher } from '@/lib/ketcher';

export const Route = createFileRoute('/_auth/experiments/$id')({
  // An experiment shows a reaction scheme, and Ketcher's first render costs ~1.2 s of
  // module fetch and WASM compile. Starting it as the route loads overlaps that with
  // everything else instead of stacking it on top.
  loader: () => {
    prewarmKetcher();
  },
  component: ExperimentPage,
});

function ExperimentPage() {
  const { id } = Route.useParams();
  return <h1 className="text-[16px]/6 font-semibold">Experiment {id} — coming soon</h1>;
}
