import { Check } from 'lucide-react';
import { useCallback, useMemo, useState } from 'react';

import { SampleResults } from '@/components/experiments/analyze-rxn/sample-results';
import { boundSampleIds, useResolveInput } from '@/components/experiments/analyze-rxn/use-resolve-input';
import { Button } from '@/components/ui/button';
import { Dialog, DialogClose, DialogContent } from '@/components/ui/dialog';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { Tabs, TabsList, TabsPanel, TabsTab } from '@/components/ui/tabs';

import type { UUID } from '@/lib/types/common.ts';
import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { Reaction } from '@/lib/types/reactions.ts';
import type { SampleCatalogFilter } from '@/lib/types/samples.ts';
import { SAMPLE_CATALOG_FILTER_LABELS, SAMPLE_CATALOG_FILTERS } from '@/lib/types/samples.ts';

/**
 * What a `SetScheme` could not match: the reactants the user drew that no registered compound
 * corresponds to, and the offer to bind each one to a sample from a catalog.
 *
 * The backend answers a scheme edit with `unresolvedInputs` — input row anchor → the molfile of
 * the molecule it created that row for. Each of those rows exists in the stoichiometry table
 * already but carries a `VIRTUAL` compound with no sample behind it, so its batch number, weight
 * and purity cannot be filled in. `ResolveInputs` is the answer, and this is where it is sent.
 *
 * One tab per unresolved row, and the tab's own substructure search runs as soon as it is shown —
 * the structure is the query, so there is nothing to type. The catalog radio is shared by every
 * tab: it is part of each search request, so changing it re-searches whatever tab is open, and
 * the results for the previous catalog stay in cache.
 *
 * Adding does not close the dialog. A reaction usually has more than one unmatched reactant, and
 * the same catalog list is what the next tab needs.
 *
 * `side="right"`, like Global Search — the other surface that searches over the page behind it.
 * The sheet keeps the stoichiometry table visible while rows are bound into it, which a centred
 * modal covers, and it takes its width from `DialogContent` rather than setting its own.
 */
export function AnalyzeRxnDialog({
  open,
  onOpenChange,
  experiment,
  reaction,
  step,
  unresolvedInputs,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  experiment: ExperimentDetails;
  /** The step being analysed, post-patch: its `inputs` are what the tabs are named after. */
  reaction: Reaction;
  /** Zero-based; the title counts from one, as the step strip does. */
  step: number;
  /** Input row anchor → the molfile the backend could not resolve. */
  unresolvedInputs: Record<UUID, string>;
}) {
  const [catalog, setCatalog] = useState<SampleCatalogFilter>('ALL');
  /** Already worded by the panel that searched — `12`, or `12+` when the catalogs cannot count. */
  const [counts, setCounts] = useState<Record<UUID, string | null>>({});

  const resolve = useResolveInput(experiment, reaction);
  const boundSamples = useMemo(() => boundSampleIds(reaction), [reaction]);

  /**
   * In `reaction.inputs` order rather than the map's, so the tabs read left to right the way the
   * reactants sit in the drawn scheme. An anchor the reaction no longer has is dropped: a scheme
   * edit that landed while the dialog was open would leave a tab naming a row that is gone.
   */
  const tabs = useMemo(
    () =>
      reaction.inputs
        .filter((input) => unresolvedInputs[input.anchor] != null)
        .map((input) => ({
          anchor: input.anchor,
          // HTML: `C<sub>6</sub>H<sub>6</sub>`, composed server-side from an element table.
          formula: input.compound.formula ?? '',
          molfile: unresolvedInputs[input.anchor],
        })),
    [reaction.inputs, unresolvedInputs],
  );

  /**
   * The selection is stored but the *active* tab is derived, so there is no state to seed and
   * none to keep in step. A dialog that has just opened has chosen nothing yet and falls through
   * to the first tab; so does one whose selected row stopped being unresolved.
   */
  const [selected, setSelected] = useState<UUID | null>(null);
  const active = tabs.some((tab) => tab.anchor === selected) ? selected : (tabs[0]?.anchor ?? null);

  const handleCountChange = useCallback((anchor: UUID, count: string | null) => {
    setCounts((previous) => (previous[anchor] === count ? previous : { ...previous, [anchor]: count }));
  }, []);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        title={`Step ${step + 1}: Analyze RXN`}
        side="right"
        /*
          Half the window, and never less than the sheet's own 720px — seven columns of catalog
          results need more room than Global Search's list of hits does. Written as one `w-*` so
          twMerge replaces the variant's width rather than adding a `min-w` that would fight it:
          a min-width beats a max-width in CSS, so `min-w-[720px]` would push the sheet past the
          edge of a window narrower than that, while `max-w-full` from the variant still clamps
          this one.
        */
        className="w-[max(720px,50vw)]"
        footer={<DialogClose render={<Button type="button" variant="secondary" size="lg" />}>Close</DialogClose>}
      >
        <RadioGroup
          value={catalog}
          onValueChange={(next) => setCatalog(next as SampleCatalogFilter)}
          aria-label="Catalog to search"
          className="flex w-auto shrink-0 items-center gap-6"
        >
          {SAMPLE_CATALOG_FILTERS.map((filter) => (
            <label key={filter} className="flex cursor-pointer items-center gap-2 text-[14px]/6">
              <RadioGroupItem value={filter} />
              {SAMPLE_CATALOG_FILTER_LABELS[filter]}
            </label>
          ))}
        </RadioGroup>

        <Tabs value={active} onValueChange={(next) => setSelected(next as UUID)} className="min-h-0 flex-1">
          <TabsList>
            {tabs.map((tab) => {
              const count = counts[tab.anchor];
              const resolved = resolve.addedInputs.has(tab.anchor);
              return (
                <TabsTab key={tab.anchor} value={tab.anchor}>
                  <span
                    className="[&_sub]:align-sub [&_sub]:text-[0.75em] [&_sub]:leading-none"
                    dangerouslySetInnerHTML={{ __html: tab.formula }}
                  />
                  {/* Only once the catalogs have answered; a tab not yet opened has no count. */}
                  {count != null && <span>({count})</span>}
                  {resolved && (
                    <>
                      <Check aria-hidden className="size-4 text-green-200" />
                      <span className="sr-only">resolved</span>
                    </>
                  )}
                </TabsTab>
              );
            })}
          </TabsList>

          {tabs.map((tab) => (
            <TabsPanel key={tab.anchor} value={tab.anchor}>
              <SampleResults
                molfile={tab.molfile}
                catalog={catalog}
                inputAnchor={tab.anchor}
                resolve={resolve}
                boundSamples={boundSamples}
                onCountChange={handleCountChange}
              />
            </TabsPanel>
          ))}
        </Tabs>
      </DialogContent>
    </Dialog>
  );
}
