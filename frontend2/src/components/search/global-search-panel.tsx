import { Search } from 'lucide-react';
import { useState } from 'react';

import { SchemeEditor } from '@/components/chemistry/scheme-editor';
import {
  EMPTY_GLOBAL_SEARCH_FORM,
  type GlobalSearchFormValues,
  isEmpty,
  STRUCTURE_TYPE_LABELS,
  toGlobalSearchRequest,
} from '@/components/search/global-search-form';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent } from '@/components/ui/dialog';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';

import type { GlobalSearchRequest, StructuralSearchType } from '@/lib/types/search.ts';

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
  /** Left unset for now — the results list is not built yet. */
  onSearch?: (request: GlobalSearchRequest) => void;
}

/**
 * The Global Search sheet: a quick text search, a structure to search by, or both.
 * Advanced search (therapeutic area, project code, author, yields, …) is not built yet.
 */
function GlobalSearchPanel({ open, onOpenChange, query, onQueryChange, onSearch }: GlobalSearchPanelProps) {
  const [structure, setStructure] = useState<Omit<GlobalSearchFormValues, 'query'>>(EMPTY_GLOBAL_SEARCH_FORM);
  const values: GlobalSearchFormValues = { ...structure, query };

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    onSearch?.(toGlobalSearchRequest(values));
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent title="Global Search" side="right" render={<form onSubmit={handleSubmit} noValidate />}>
        <div className="flex flex-col gap-4 rounded-6 bg-neutral-100 p-4">
          <label className="flex h-12 items-center gap-2 rounded-full border border-blue-10 bg-blue-5 px-4">
            <Search className="size-5 shrink-0 text-neutral-700" />
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
            {structure.structure && (
              <div className="flex items-center gap-4">
                <RadioGroup
                  value={structure.structureType}
                  onValueChange={(next) => setStructure({ ...structure, structureType: next as StructuralSearchType })}
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
                  onClick={() => setStructure({ ...structure, structure: null, isReaction: false })}
                >
                  Clear
                </Button>
              </div>
            )}
          </div>

          <SchemeEditor
            value={structure.structure}
            onChange={(next) =>
              setStructure({ ...structure, structure: next?.structure ?? null, isReaction: next?.isReaction ?? false })
            }
          />

          <div className="flex justify-end gap-3">
            <Button
              type="button"
              variant="secondary"
              size="lg"
              onClick={() => {
                setStructure(EMPTY_GLOBAL_SEARCH_FORM);
                onQueryChange('');
              }}
            >
              Clear All
            </Button>
            <Button type="submit" size="lg" disabled={isEmpty(values)}>
              Search
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}

export { GlobalSearchPanel };
