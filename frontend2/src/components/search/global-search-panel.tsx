import { Search } from 'lucide-react';
import type { SubmitEvent } from 'react';
import { useEffect, useState } from 'react';

import { SchemeEditor } from '@/components/chemistry/scheme-editor';
import { AdvancedSearch } from '@/components/search/advanced-search';
import {
  EMPTY_GLOBAL_SEARCH_FORM,
  type GlobalSearchFormValues,
  isEmpty,
  toGlobalSearchRequest,
} from '@/components/search/global-search-form';
import { SearchResults } from '@/components/search/search-results';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent } from '@/components/ui/dialog';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';
import { useGlobalSearch } from '@/lib/api/search';

import type { GlobalSearchRequest, StructuralSearchType } from '@/lib/types/search.ts';
import { STRUCTURE_TYPE_LABELS } from '@/lib/types/search.ts';

interface GlobalSearchPanelProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /**
   * The term, owned by AppHeader: the box up there and the quick-search box in here are
   * two views of the same search, which is also what carries whatever was typed in the
   * header before Enter into the sheet.
   */
  query: string;
  onQueryChange: (query: string) => void;
  /** Optional: the sheet renders its own results, so this is only for observing them. */
  onSearch?: (request: GlobalSearchRequest) => void;
}

/** Everything the sheet owns itself: the term lives one level up, in AppHeader. */
type OwnValues = Omit<GlobalSearchFormValues, 'query'>;

/**
 * The Global Search sheet: a quick text search, a structure to search by, advanced
 * filters, or any combination, with the results below the form.
 */
function GlobalSearchPanel({ open, onOpenChange, query, onQueryChange, onSearch }: GlobalSearchPanelProps) {
  const [own, setOwn] = useState<OwnValues>(EMPTY_GLOBAL_SEARCH_FORM);
  const [advancedOpen, setAdvancedOpen] = useState(false);
  // Bumped by Clear All to remount AdvancedSearch, which is the only way to reset the state
  // its fields keep to themselves — a half-typed combobox term, a chosen numeric operator
  // still waiting for a number.
  const [generation, setGeneration] = useState(0);
  // The request as submitted, which is what the results belong to — editing the form does
  // not disturb them until Search is pressed again.
  const [submitted, setSubmitted] = useState<GlobalSearchRequest | null>(null);

  const values: GlobalSearchFormValues = { ...own, query };
  const results = useGlobalSearch(submitted);

  function patch(next: Partial<GlobalSearchFormValues>) {
    setOwn((previous) => ({ ...previous, ...next }));
  }

  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitted(toGlobalSearchRequest(values));
    // Collapsing hands the space back to the results and leaves the summary as the record
    // of what was searched for, as in indigo-frontend.
    setAdvancedOpen(false);
  }

  // Opening the sheet with something already to search for — Enter in the header box
  // carries its term across — means the search has been asked for already, so run it
  // rather than showing a filled-in form with an untouched Search button. The gate is the
  // button's own: if Search would be clickable on open, it counts as pressed.
  //
  // Adjusted during render, the pattern React documents for reacting to a changed prop:
  // an effect would submit a render late, and would have to distinguish the opening from
  // every later keystroke in the sheet's own box, which must not search on its own. The
  // initial `false` counts a panel mounted already open as an opening, since the sheet in
  // AppHeader mounts closed and anything mounting it open is opening it.
  const [wasOpen, setWasOpen] = useState(false);
  if (open !== wasOpen) {
    setWasOpen(open);
    if (open && !isEmpty(values)) setSubmitted(toGlobalSearchRequest(values));
  }

  // One place to report a submitted search, whichever of the two started it. The
  // observer is not part of running the search, so it belongs here rather than in
  // either trigger.
  useEffect(() => {
    if (submitted) onSearch?.(submitted);
  }, [submitted, onSearch]);

  function clearAll() {
    setOwn(EMPTY_GLOBAL_SEARCH_FORM);
    onQueryChange('');
    setSubmitted(null);
    setGeneration((previous) => previous + 1);
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent title="Global Search" side="right" render={<form onSubmit={handleSubmit} noValidate />}>
        <div className="flex flex-col gap-4 rounded-6 bg-neutral-100 p-4">
          <label className="flex h-12 items-center gap-2 rounded-full border border-blue-10 bg-blue-5 px-4">
            <Search aria-hidden className="size-5 shrink-0 text-neutral-700" />
            <input
              type="search"
              value={query}
              onChange={(event) => onQueryChange(event.target.value)}
              placeholder="Type Request"
              aria-label="Quick search"
              className="w-full bg-transparent text-[14px]/6 outline-none placeholder:text-neutral-700"
            />
          </label>

          <div className="flex min-h-8 items-center justify-between gap-4">
            <span className="text-[14px]/6 text-neutral-800">or Draw Structure</span>
            {/* Only meaningful once there is a structure to match against. */}
            {own.structure && (
              <div className="flex items-center gap-4">
                <RadioGroup
                  value={own.structureType}
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
                <Button
                  type="button"
                  variant="link"
                  size="lg"
                  onClick={() => patch({ structure: null, isReaction: false, reactionRole: null })}
                >
                  Clear
                </Button>
              </div>
            )}
          </div>

          <SchemeEditor
            value={own.structure}
            onChange={(next) => {
              const isReaction = next?.isReaction ?? false;
              patch({
                structure: next?.structure ?? null,
                isReaction,
                // Reaction Role applies to a drawn molecule only, so anything else clears
                // it rather than leaving a value the request would have to drop silently.
                reactionRole: next && !isReaction ? own.reactionRole : null,
              });
            }}
          />

          <AdvancedSearch
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
            <Button type="submit" size="lg" disabled={isEmpty(values)}>
              Search
            </Button>
          </div>
        </div>

        {submitted && <SearchResults query={results} onSelect={() => onOpenChange(false)} />}
      </DialogContent>
    </Dialog>
  );
}

export { GlobalSearchPanel };
