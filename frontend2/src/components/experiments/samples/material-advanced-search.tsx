import { ChevronDown, Info } from 'lucide-react';

import { DictionaryCombobox } from '@/components/common/dictionary-combobox';
import type { AddMaterialFormValues, MaterialFilter } from '@/components/experiments/samples/add-material-form';
import {
  isFilterDisabled,
  MATERIAL_FILTER_LABELS,
  PUBCHEM_NOTICE,
  pubchemIncluded,
  summarizeAddMaterialSearch,
} from '@/components/experiments/samples/add-material-form';
import { NumericSearchField } from '@/components/ui/search/numeric-search-field';
import { TextSearchField } from '@/components/ui/search/text-search-field';
import { Collapsible, CollapsiblePanel, CollapsibleTrigger } from '@/components/ui/collapsible';
import { Field } from '@/components/ui/field';
import { cn } from '@/lib/utils';

interface MaterialAdvancedSearchProps {
  values: AddMaterialFormValues;
  /** Patches one or more fields; the panel never owns the values it edits. */
  onChange: (patch: Partial<AddMaterialFormValues>) => void;
  /** Controlled, so submitting the search can collapse the panel back to its summary. */
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

/** `id` and `htmlFor` for one filter's control — "material-search-compoundKey". */
function fieldId(filter: MaterialFilter): string {
  return `material-search-${filter}`;
}

/**
 * The Advanced search section of Add Material: collapsed by default, showing what is selected
 * next to its title, and expanding to a two-column grid of ten filters.
 *
 * Everything is laid out the same way `search/advanced-search.tsx` lays out Global Search's, so
 * the two sheets read as one family. What differs is the catalog gate: choosing a catalog that
 * reaches PubChem disables all but Molecular Formula, and says why below them rather than
 * leaving nine greyed boxes to be puzzled over. Disabled fields **keep their values** — coming
 * back to Indigo ELN restores the search rather than making it be retyped.
 */
function MaterialAdvancedSearch({ values, onChange, open, onOpenChange }: MaterialAdvancedSearchProps) {
  const summary = summarizeAddMaterialSearch(values);
  const disabled = (filter: MaterialFilter) => isFilterDisabled(values.catalog, filter);

  return (
    <Collapsible
      open={open}
      onOpenChange={onOpenChange}
      className="rounded-6 border border-neutral-300 bg-neutral-000 px-4 py-3"
    >
      <CollapsibleTrigger className="rounded-2">
        <span className="shrink-0 text-[16px]/6 text-neutral-1000">
          Advanced search{!open && summary.length > 0 && ` (${summary.length})`}
        </span>
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
          <TextFilter filter="compoundKey" values={values} onChange={onChange} disabled={disabled('compoundKey')} />
          <TextFilter
            filter="nbkBatchNumber"
            values={values}
            onChange={onChange}
            disabled={disabled('nbkBatchNumber')}
          />

          <TextFilter
            filter="molecularFormula"
            values={values}
            onChange={onChange}
            disabled={disabled('molecularFormula')}
          />
          <Field id={fieldId('molWeight')} label={MATERIAL_FILTER_LABELS.molWeight}>
            <NumericSearchField
              id={fieldId('molWeight')}
              label={MATERIAL_FILTER_LABELS.molWeight}
              value={values.molWeight}
              onValueChange={(molWeight) => onChange({ molWeight })}
              disabled={disabled('molWeight')}
            />
          </Field>

          <TextFilter filter="chemicalName" values={values} onChange={onChange} disabled={disabled('chemicalName')} />
          <TextFilter
            filter="externalNumber"
            values={values}
            onChange={onChange}
            disabled={disabled('externalNumber')}
          />

          <Field id={fieldId('compoundState')} label={MATERIAL_FILTER_LABELS.compoundState}>
            <DictionaryCombobox
              id={fieldId('compoundState')}
              dictionary="COMPONENT_STATE"
              value={values.compoundState}
              onValueChange={(compoundState) => onChange({ compoundState })}
              disabled={disabled('compoundState')}
            />
          </Field>
          <TextFilter filter="batchComment" values={values} onChange={onChange} disabled={disabled('batchComment')} />

          <Field id={fieldId('healthHazards')} label={MATERIAL_FILTER_LABELS.healthHazards}>
            <DictionaryCombobox
              id={fieldId('healthHazards')}
              dictionary="HEALTH_HAZARD"
              value={values.healthHazards}
              onValueChange={(healthHazards) => onChange({ healthHazards })}
              disabled={disabled('healthHazards')}
            />
          </Field>
          <TextFilter filter="casNumber" values={values} onChange={onChange} disabled={disabled('casNumber')} />

          {pubchemIncluded(values.catalog) && (
            <p className="col-span-2 flex items-start gap-2 rounded-md border border-blue-10 bg-blue-5 px-4 py-3 text-[14px]/6 text-neutral-1000">
              <Info aria-hidden className="mt-1 size-4 shrink-0 text-blue-400" />
              {PUBCHEM_NOTICE}
            </p>
          )}
        </div>
      </CollapsiblePanel>
    </Collapsible>
  );
}

/** One of the seven `TextSearch` filters — they differ only by which key they read and write. */
function TextFilter({
  filter,
  values,
  onChange,
  disabled,
}: {
  filter: Extract<
    MaterialFilter,
    | 'compoundKey'
    | 'nbkBatchNumber'
    | 'molecularFormula'
    | 'chemicalName'
    | 'externalNumber'
    | 'batchComment'
    | 'casNumber'
  >;
  values: AddMaterialFormValues;
  onChange: (patch: Partial<AddMaterialFormValues>) => void;
  disabled: boolean;
}) {
  const label = MATERIAL_FILTER_LABELS[filter];

  return (
    <Field id={fieldId(filter)} label={label}>
      <TextSearchField
        id={fieldId(filter)}
        label={label}
        value={values[filter]}
        onValueChange={(value) => onChange({ [filter]: value })}
        disabled={disabled}
      />
    </Field>
  );
}

export { MaterialAdvancedSearch };
