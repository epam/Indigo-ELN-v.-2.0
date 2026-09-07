import { Search } from 'lucide-react';
import type { SubmitEvent } from 'react';
import { useCallback, useMemo, useState } from 'react';

import { SchemeEditor } from '@/components/chemistry/scheme-editor';
import type { StructureEditorResult } from '@/components/chemistry/structure-editor-dialog';
import type { AddMaterialFormValues } from '@/components/experiments/samples/add-material-form';
import {
  EMPTY_ADD_MATERIAL_FORM,
  isEmpty,
  REACTION_NOT_SEARCHABLE,
  toFindSamplesRequest,
} from '@/components/experiments/samples/add-material-form';
import { MaterialAdvancedSearch } from '@/components/experiments/samples/material-advanced-search';
import { SampleResults } from '@/components/experiments/samples/sample-results';
import { STRUCTURE_TYPE_LABELS } from '@/components/search/global-search-form';
import { Button } from '@/components/ui/button';
import { Dialog, DialogClose, DialogContent } from '@/components/ui/dialog';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { useAddMaterial } from '@/lib/hooks/experiments/use-add-material';
import { getAllInputSampleIds } from '@/lib/reactions';
import { notifyError } from '@/lib/toast';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { Reaction } from '@/lib/types/reactions.ts';
import type { FindSamplesRequest, SampleCatalogFilter } from '@/lib/types/samples.ts';
import { SAMPLE_CATALOG_FILTER_LABELS, SAMPLE_CATALOG_FILTERS } from '@/lib/types/samples.ts';
import type { StructuralSearchType } from '@/lib/types/search.ts';

/**
 * Add Material: search the catalogs for a registered compound and append it to the Reactants,
 * Reagents, Solvents table as a new input row.
 *
 * The counterpart of the toolbar's plain `+`, which adds an empty row on an `UNKNOWN` compound.
 * Here the row arrives with a sample behind it, so its batch number, molecular weight and purity
 * are filled in by the server rather than left to be typed.
 *
 * Structurally this is Global Search's form over Analyze RXN's results, and deliberately so:
 * both halves are the shared components, and the sheet only assembles them. Three things are
 * particular to it:
 *
 * - **Nothing is searched until Search is pressed.** Analyze RXN has a structure to search for
 *   the moment it opens; this has an empty form, and every keystroke is not a question.
 * - **Search needs a criterion.** The backend would take a request carrying only a catalog, but
 *   a whole catalog answers nothing that was asked, so `isEmpty` holds the button until there is
 *   a term, a structure or a filter that will actually be sent.
 * - **The catalog gates the form.** See `add-material-form.ts`: a catalog reaching PubChem
 *   disables all but Molecular Formula, and those filters are dropped from the request.
 *
 * Adding does not close the sheet — a step usually gains several materials in one visit — and
 * the mutation's patch fills the row into the table behind it, which is why this is a
 * `side="right"` sheet rather than a centred modal.
 */
export function AddMaterialDialog({
  open,
  onOpenChange,
  experiment,
  reaction,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  experiment: ExperimentDetails;
  /** The step the chosen material is appended to. */
  reaction: Reaction;
}) {
  const [values, setValues] = useState<AddMaterialFormValues>(EMPTY_ADD_MATERIAL_FORM);
  const [advancedOpen, setAdvancedOpen] = useState(false);
  // Bumped by Clear All to remount the advanced panel, which is the only way to reset the state
  // its fields keep to themselves — an operator chosen on a box still waiting for a value.
  const [generation, setGeneration] = useState(0);
  // The request as submitted, which is what the results belong to — editing the form does not
  // disturb them until Search is pressed again.
  const [submitted, setSubmitted] = useState<FindSamplesRequest | null>(null);
  const [count, setCount] = useState<string | null>(null);

  const addMaterial = useAddMaterial(experiment, reaction);
  const boundSamples = useMemo(() => getAllInputSampleIds(reaction), [reaction]);
  // `SampleResults` reports through this on every count change, so it has to keep its identity
  // or the effect behind it would loop.
  const handleCountChange = useCallback((next: string | null) => setCount(next), []);

  function patch(next: Partial<AddMaterialFormValues>) {
    setValues((previous) => ({ ...previous, ...next }));
  }

  /**
   * Takes the drawing, or refuses it. Throwing is what holds the sketcher open with the drawing
   * intact — `StructureEditorDialog` swallows a rejection from `onSave` on the grounds that
   * whoever rejected has already reported it, which is what the toast above is for.
   */
  function handleStructure(next: StructureEditorResult | null) {
    if (next?.isReaction) {
      const error = new Error(REACTION_NOT_SEARCHABLE);
      notifyError(error);
      throw error;
    }
    patch({ structure: next?.structure ?? null });
  }

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitted(toFindSamplesRequest(values));
    // Collapsing hands the space back to the results and leaves the summary as the record of
    // what was searched for, as in indigo-frontend.
    setAdvancedOpen(false);
  }

  function clearAll() {
    setValues(EMPTY_ADD_MATERIAL_FORM);
    setSubmitted(null);
    setCount(null);
    setGeneration((previous) => previous + 1);
  }

  return (
    <Dialog
      open={open}
      onOpenChange={onOpenChange}
      /* Reset after the exit transition rather than during it, so the form is never seen
         emptying as the sheet slides out. The sheet is mounted permanently — that is what lets
         it slide out at all — so without this it would keep the last visit's search forever. */
      onOpenChangeComplete={(nextOpen) => {
        if (nextOpen) return;
        clearAll();
        setAdvancedOpen(false);
      }}
    >
      <DialogContent
        title="Add Material"
        side="right"
        /* Half the window, never below the sheet's own 720px — the same seven result columns
           Analyze RXN sizes itself for. One `w-*`, so twMerge replaces the variant's width. */
        className="w-[max(720px,50vw)]"
        render={<form onSubmit={handleSubmit} noValidate />}
        footer={<DialogClose render={<Button type="button" variant="secondary" size="lg" />}>Close</DialogClose>}
      >
        <div className="flex shrink-0 flex-col gap-4 rounded-6 bg-neutral-100 p-4">
          <label className="flex h-12 items-center gap-2 rounded-full border border-blue-10 bg-blue-5 px-4">
            <Search className="size-5 shrink-0 text-neutral-700" />
            <input
              type="search"
              value={values.quickSearch}
              onChange={(event) => patch({ quickSearch: event.target.value })}
              placeholder="Type Request"
              aria-label="Quick search"
              className="w-full bg-transparent text-[14px]/6 outline-none placeholder:text-neutral-700"
            />
          </label>

          <RadioGroup
            value={values.catalog}
            onValueChange={(next) => patch({ catalog: next as SampleCatalogFilter })}
            aria-label="Catalog to search"
            className="flex w-auto flex-wrap items-center gap-6"
          >
            {SAMPLE_CATALOG_FILTERS.map((filter) => (
              <label key={filter} className="flex cursor-pointer items-center gap-2 text-[14px]/6">
                <RadioGroupItem value={filter} />
                {SAMPLE_CATALOG_FILTER_LABELS[filter]}
              </label>
            ))}
          </RadioGroup>

          <div className="flex min-h-8 items-center justify-between gap-4">
            <span className="text-[14px]/6 text-neutral-800">or Draw Structure</span>
            {/* Only meaningful once there is a structure to match against. */}
            {values.structure && (
              <div className="flex items-center gap-4">
                <RadioGroup
                  value={values.structureType}
                  onValueChange={(next) => patch({ structureType: next as StructuralSearchType })}
                  aria-label="Structure search type"
                  className="flex w-auto items-center gap-4"
                >
                  {(Object.keys(STRUCTURE_TYPE_LABELS) as StructuralSearchType[]).map((type) => (
                    <label key={type} className="flex cursor-pointer items-center gap-2 text-[14px]/6">
                      <RadioGroupItem value={type} />
                      {STRUCTURE_TYPE_LABELS[type]}
                    </label>
                  ))}
                </RadioGroup>
                <Button type="button" variant="link" size="lg" onClick={() => patch({ structure: null })}>
                  Clear
                </Button>
              </div>
            )}
          </div>

          {/* A catalog holds compounds, so a drawn reaction is refused rather than searched
              for — see `handleStructure`. */}
          <SchemeEditor value={values.structure} onChange={handleStructure} />

          <MaterialAdvancedSearch
            key={generation}
            values={values}
            onChange={patch}
            open={advancedOpen}
            onOpenChange={setAdvancedOpen}
          />

          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" size="lg" onClick={clearAll}>
              Clear All
            </Button>
            {/* A catalog on its own asks nothing; see `isEmpty` for why a filter the catalog
                has disabled does not count as a criterion either. */}
            <Button type="submit" size="lg" disabled={isEmpty(values)}>
              Search
            </Button>
          </div>
        </div>

        {submitted != null && (
          <>
            {count != null && <p className="shrink-0 text-[14px]/6 text-neutral-800">{count} materials found</p>}
            <SampleResults
              request={submitted}
              onAdd={addMaterial.add}
              addingRows={addMaterial.addingRows}
              boundSamples={boundSamples}
              onCountChange={handleCountChange}
            />
          </>
        )}
      </DialogContent>
    </Dialog>
  );
}
