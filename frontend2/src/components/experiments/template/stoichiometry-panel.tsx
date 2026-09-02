import { Download, MoreHorizontal, Plus, Upload } from 'lucide-react';

import { CollapsibleCard } from '@/components/common/collapsible-card';
import { ReactionProductsTable } from '@/components/experiments/stoichiometry/products-table';
import { StoichiometryTable } from '@/components/experiments/stoichiometry/stoichiometry-table';
import { ReactionSchemePanel } from '@/components/experiments/template/reaction-scheme-panel';
import { Button } from '@/components/ui/button';
import { useReactionStep } from '@/lib/hooks/use-reaction-step';
import { cn } from '@/lib/utils';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { TemplateComponent } from '@/lib/types/templates.ts';

type StoichiometryComponent = Extract<TemplateComponent, { type: 'stoichiometryTable' }>;

const STEP_CLASS = 'flex items-center gap-2 border-r border-neutral-300 px-4 py-2 text-[14px]/6';
const STEP_ACTIVE_CLASS = 'bg-card font-semibold text-blue-400';
const STEP_INACTIVE_CLASS = 'text-neutral-800';

/**
 * The step strip is built and gated off rather than deleted. Every control on it needs a
 * mutation the backend does not have — there is no AddReaction, and no rename or delete —
 * so all it could do today is switch between steps nothing can create. Turn this on with the
 * mutations, and give `useReactionStep` real state at the same time.
 */
const SHOW_STEP_SELECTOR = false;

/**
 * The `stoichiometryTable` template component, and the only one that renders its own card — the
 * step strip sits *above* the card in the design, so this owns both.
 *
 * Which reaction the blocks below show comes from `useReactionStep`, which is `0` while the
 * strip is gated off. `model.reactions` is `@NotEmpty` on the backend, so indexing it is safe.
 *
 * It takes the whole experiment rather than just `model.reactions` because the scheme block
 * writes: the mutation is keyed by experiment id, and edit rights come off `currentPermissions`.
 */
export function StoichiometryPanel({
  component,
  experiment,
}: {
  component: StoichiometryComponent;
  experiment: ExperimentDetails;
}) {
  const reactions = experiment.model.reactions;
  const step = useReactionStep();
  const reaction = reactions[step];

  return (
    <div className="flex flex-col gap-4">
      {SHOW_STEP_SELECTOR && (
        <div className="flex items-stretch overflow-hidden rounded-6 bg-neutral-200 shadow-card">
          {reactions.map((each, index) => (
            <div key={each.anchor} className={cn(STEP_CLASS, index === step ? STEP_ACTIVE_CLASS : STEP_INACTIVE_CLASS)}>
              {/* TODO(reaction-steps): selecting a step needs `useReactionStep` to hold state. */}
              <span>{index + 1} step</span>
              {/* Rename and delete live behind here; both are mutations. */}
              {index === step && (
                <Button variant="ghost" size="icon-xs" aria-label={`Step ${index + 1} actions`} disabled>
                  <MoreHorizontal />
                </Button>
              )}
            </div>
          ))}
          {/* TODO(add-reaction-step): needs a mutation to create the reaction. */}
          <Button variant="ghost" size="icon" aria-label="Add step" className="m-1 rounded-2" disabled>
            <Plus />
          </Button>
        </div>
      )}

      <CollapsibleCard
        title="Stoichiometric Calculation"
        actions={
          <>
            {/* TODO(export-sdf): GET /experiments/{id}/exportSdf. */}
            <Button variant="ghost" size="icon" aria-label="Export SDF" disabled>
              <Download />
            </Button>
            {/* TODO(import-sdf): POST /experiments/{id}/datamodel/reactions/{anchor}/importSDF. */}
            <Button variant="ghost" size="icon" aria-label="Import SDF" disabled>
              <Upload />
            </Button>
          </>
        }
      >
        <div className="flex flex-col gap-4">
          {/* Each block is declared by the template, so a template that asks for none renders none. */}
          {component.reactionScheme && (
            <section className="flex flex-col gap-2">
              <h3 className="text-[14px]/6 text-neutral-800">Reaction Scheme</h3>
              <ReactionSchemePanel experiment={experiment} reaction={reaction} />
            </section>
          )}

          {component.reactantsReagentsSolvents && (
            <section className="flex flex-col gap-2">
              <h3 className="text-[14px]/6 text-neutral-800">Reactants, Reagents, Solvents</h3>
              <StoichiometryTable experiment={experiment} reaction={reaction} />
            </section>
          )}

          {/*
            The template flag is `intendedProducts` and the heading is "Reaction Products" — the
            same pair indigo-frontend has. `intended` is what filters the rows; the block is the
            products table.
          */}
          {component.intendedProducts && (
            <section className="flex flex-col gap-2">
              <h3 className="text-[14px]/6 text-neutral-800">Reaction Products</h3>
              <ReactionProductsTable experiment={experiment} step={step} />
            </section>
          )}
        </div>
      </CollapsibleCard>
    </div>
  );
}
