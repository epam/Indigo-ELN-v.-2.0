import { Download, MoreHorizontal, Plus, Upload } from 'lucide-react';
import { useState } from 'react';

import { CollapsibleCard } from '@/components/common/collapsible-card';
import { TemplatePlaceholder } from '@/components/experiments/template/template-placeholder';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

import type { Reaction } from '@/lib/types/experiments.ts';
import type { TemplateComponent } from '@/lib/types/templates.ts';

type StoichiometryComponent = Extract<TemplateComponent, { type: 'stoichiometryTable' }>;

const STEP_CLASS = 'flex items-center gap-2 border-r border-neutral-300 px-4 py-2 text-[14px]/6';
const STEP_ACTIVE_CLASS = 'bg-card font-semibold text-blue-400';
const STEP_INACTIVE_CLASS = 'text-neutral-800';

/**
 * The `stoichiometryTable` template component, and the only one that renders its own card — the
 * step strip sits *above* the card in the design, so this owns both.
 *
 * The strip picks which reaction the table below shows. `model.reactions` is that list: one step
 * per reaction, and today every experiment has exactly one. Adding a step needs a mutation, so
 * `+` is inert for now.
 */
export function StoichiometryPanel({
  component,
  reactions,
}: {
  component: StoichiometryComponent;
  reactions: Reaction[];
}) {
  const [step, setStep] = useState(0);

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-stretch overflow-hidden rounded-6 bg-neutral-200 shadow-card">
        {reactions.map((reaction, index) => (
          <div
            key={reaction.anchor}
            className={cn(STEP_CLASS, index === step ? STEP_ACTIVE_CLASS : STEP_INACTIVE_CLASS)}
          >
            <button type="button" onClick={() => setStep(index)} className="cursor-pointer outline-none">
              {index + 1} step
            </button>
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
              {/* TODO(reaction-scheme): `SchemeEditor` over the reaction's rxnfile. */}
              <TemplatePlaceholder>The reaction scheme goes here.</TemplatePlaceholder>
            </section>
          )}

          {component.reactantsReagentsSolvents && (
            <section className="flex flex-col gap-2">
              <h3 className="text-[14px]/6 text-neutral-800">Reactants, Reagents, Solvents</h3>
              {/* TODO(stoichiometry-table): the editable inputs table, driven by the mutation API. */}
              <TemplatePlaceholder>The reactants, reagents and solvents table goes here.</TemplatePlaceholder>
            </section>
          )}

          {component.intendedProducts && (
            <section className="flex flex-col gap-2">
              <h3 className="text-[14px]/6 text-neutral-800">Intended Products</h3>
              {/* TODO(intended-products): the outputs table. */}
              <TemplatePlaceholder>The intended products table goes here.</TemplatePlaceholder>
            </section>
          )}
        </div>
      </CollapsibleCard>
    </div>
  );
}
