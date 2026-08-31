import { createFileRoute } from '@tanstack/react-router';

import { ExperimentHeader } from '@/components/experiments/experiment-header';
import { activeTabIndex, tabSlugs } from '@/components/experiments/experiment-template';
import { ExperimentTemplateTab } from '@/components/experiments/experiment-template-tab';
import { ExperimentPageSkeleton } from '@/components/experiments/experiment-page-skeleton';
import { useExperiment } from '@/lib/api/experiments';
import { useTemplate } from '@/lib/api/templates';
import { prewarmKetcher } from '@/lib/ketcher';
import { z } from '@/lib/zod';

/**
 * The tab is a search param rather than a path segment because the tabs are template data: their
 * names — and so their slugs — are only known once the template has loaded, which a path route
 * would have to be able to enumerate up front. An unknown slug resolves to the first tab rather
 * than 404ing, so a renamed tab degrades instead of breaking a bookmark.
 */
const searchSchema = z.object({
  tab: z.string().optional(),
});

export const Route = createFileRoute('/_auth/experiments/$id')({
  validateSearch: searchSchema,
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
  const { tab } = Route.useSearch();

  const { data: experiment, isPending, isError } = useExperiment(id);
  // The second of two chained queries: the template id only exists once the experiment has
  // loaded, and it is what says which tabs this experiment has and what is in them.
  const { data: template } = useTemplate(experiment?.templateId);

  if (isPending) return <ExperimentPageSkeleton />;
  // apiFetch has already toasted the failure; this is the page saying what it cannot show.
  if (isError || !experiment) {
    return <p className="text-[14px]/6 text-destructive">This experiment could not be loaded.</p>;
  }

  const tabs = template?.templateTabs;
  const index = tabs ? activeTabIndex(tabs, tab) : 0;

  return (
    <>
      <ExperimentHeader
        experimentId={id}
        experiment={experiment}
        tabs={tabs}
        activeTab={tabs ? tabSlugs(tabs)[index] : undefined}
      />
      {tabs && tabs.length > 0 && <ExperimentTemplateTab tab={tabs[index]} experiment={experiment} />}
    </>
  );
}
