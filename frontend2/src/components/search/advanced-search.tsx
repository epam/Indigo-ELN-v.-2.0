import { ChevronDown } from 'lucide-react';

import { DictionaryCombobox } from '@/components/common/dictionary-combobox';
import { AuthorCombobox } from '@/components/ui/search/author-combobox';
import {
  type GlobalSearchFormValues,
  showReactionRole,
  summarizeAdvancedSearch,
} from '@/components/search/global-search-form';
import { NumericSearchField } from '@/components/ui/search/numeric-search-field';
import { Collapsible, CollapsiblePanel, CollapsibleTrigger } from '@/components/ui/collapsible';
import { Combobox } from '@/components/ui/combobox';
import { Field } from '@/components/ui/field';
import type { ExperimentStatus } from '@/lib/types/experiments.ts';
import { EXPERIMENT_STATUS_LABELS, EXPERIMENT_STATUSES } from '@/lib/types/experiments.ts';
import type { ReactionRole } from '@/lib/types/search.ts';
import { REACTION_ROLE_LABELS, REACTION_ROLES } from '@/lib/types/search.ts';
import { cn } from '@/lib/utils';

interface AdvancedSearchProps {
  values: GlobalSearchFormValues;
  /** Patches one or more fields; the panel never owns the values it edits. */
  onChange: (patch: Partial<GlobalSearchFormValues>) => void;
  /** Controlled, so submitting the search can collapse the panel back to its summary. */
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

/**
 * The Advanced Search section: collapsed by default, showing what is selected next to its
 * title, and expanding to a two-column grid of filters.
 *
 * Reaction Role only appears for a drawn molecule — see `showReactionRole`. The Angular
 * original kept its slot with `invisible`; not rendering it is cleaner in a grid, and the
 * summary applies the same predicate so the two can never disagree.
 */
function AdvancedSearch({ values, onChange, open, onOpenChange }: AdvancedSearchProps) {
  const summary = summarizeAdvancedSearch(values);

  return (
    <Collapsible
      open={open}
      onOpenChange={onOpenChange}
      className="rounded-6 border border-neutral-300 bg-neutral-000 px-4 py-3"
    >
      <CollapsibleTrigger className="rounded-2">
        <span className="shrink-0 text-[16px]/6 text-neutral-1000">Advanced Search</span>
        {/*
          Only while collapsed: expanded, the controls themselves are the summary, and
          repeating it above them just competes with the labels.
        */}
        {!open && summary.length > 0 && (
          <span className="flex min-w-0 flex-wrap items-baseline gap-x-4 gap-y-1 text-[12px]/5 text-neutral-1000">
            {summary.map((item) => (
              <span key={item.label} className="truncate">
                <b className="font-semibold">{item.label}</b> <span className="text-neutral-700">{item.operator}</span>{' '}
                {item.value}
              </span>
            ))}
          </span>
        )}
        <ChevronDown
          aria-hidden
          className={cn(
            'ml-auto size-5 shrink-0 text-neutral-700 transition-transform duration-150',
            open && 'rotate-180',
          )}
        />
      </CollapsibleTrigger>

      <CollapsiblePanel>
        <div className="grid grid-cols-2 gap-x-8 gap-y-3 pt-3">
          <Field id="search-therapeutic-area" label="Therapeutic Area">
            <DictionaryCombobox
              id="search-therapeutic-area"
              dictionary="THERAPEUTIC_AREA"
              value={values.therapeuticArea}
              onValueChange={(therapeuticArea) => onChange({ therapeuticArea })}
            />
          </Field>
          <Field id="search-project-code" label="Project Code">
            <DictionaryCombobox
              id="search-project-code"
              dictionary="PROJECT_CODE"
              value={values.projectCode}
              onValueChange={(projectCode) => onChange({ projectCode })}
            />
          </Field>

          <Field id="search-batch-yield" label="Batch Yield, %">
            <NumericSearchField
              id="search-batch-yield"
              label="Batch Yield, %"
              value={values.batchYield}
              onValueChange={(batchYield) => onChange({ batchYield })}
            />
          </Field>
          <Field id="search-batch-purity" label="Batch Purity, %">
            <NumericSearchField
              id="search-batch-purity"
              label="Batch Purity, %"
              value={values.batchPurity}
              onValueChange={(batchPurity) => onChange({ batchPurity })}
            />
          </Field>

          <Field id="search-author" label="Author" className="col-span-2">
            <AuthorCombobox id="search-author" value={values.author} onValueChange={(author) => onChange({ author })} />
          </Field>

          <Field id="search-experiment-status" label="Experiment Status">
            <Combobox<ExperimentStatus>
              id="search-experiment-status"
              value={values.experimentStatus}
              onValueChange={(experimentStatus) => onChange({ experimentStatus })}
              items={[...EXPERIMENT_STATUSES]}
              itemToLabel={(status) => EXPERIMENT_STATUS_LABELS[status]}
            />
          </Field>
          {showReactionRole(values) && (
            <Field id="search-reaction-role" label="Reaction Role">
              <Combobox<ReactionRole>
                id="search-reaction-role"
                value={values.reactionRole}
                onValueChange={(reactionRole) => onChange({ reactionRole })}
                items={[...REACTION_ROLES]}
                itemToLabel={(role) => REACTION_ROLE_LABELS[role]}
              />
            </Field>
          )}
        </div>
      </CollapsiblePanel>
    </Collapsible>
  );
}

export { AdvancedSearch };
