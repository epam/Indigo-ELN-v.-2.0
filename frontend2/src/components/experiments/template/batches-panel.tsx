import { ProductBatchSummaryTable } from '@/components/experiments/stoichiometry/batches-table';
import { CollapsibleCard } from '@/components/common/collapsible-card';
import { useReactionStep } from '@/lib/hooks/use-reaction-step';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';

/**
 * The `batches` template component — the Product Batch Summary.
 *
 * It brings its own card rather than being wrapped: `COMPONENT_TITLES` maps it to `null`, matching
 * indigo-frontend, where the summary table is one of the two components rendered outside the
 * collapsible wrapper. It still folds, like every other panel on the screen — the wrapper is what
 * it does without, not the behaviour.
 *
 * Which reaction's batches are shown comes from `useReactionStep`, which is `0` while the step
 * strip in `StoichiometryPanel` is gated off. `model.reactions` is `@NotEmpty` on the backend, so
 * indexing it is safe.
 */
export function BatchesPanel({ experiment }: { experiment: ExperimentDetails }) {
  const step = useReactionStep();
  const reaction = experiment.model.reactions[step];

  return (
    <CollapsibleCard title="Product Batch Summary">
      <ProductBatchSummaryTable experiment={experiment} reaction={reaction} step={step} />
    </CollapsibleCard>
  );
}
