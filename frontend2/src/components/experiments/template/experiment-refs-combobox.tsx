import { useState } from 'react';

import { MultiCombobox } from '@/components/ui/combobox';
import { useExperimentSuggestions } from '@/lib/api/experiments';

import type { ExperimentRef } from '@/lib/types/experiments.ts';

function refKey(ref: ExperimentRef): string {
  return ref.id;
}

/**
 * Experiments referenced from another one — Linked Experiment, Cont. TO Rxn and Cont. FROM Rxn are
 * three instances of this.
 *
 * `experiments/suggest` matches prefixes and caps at ten, so unlike the dictionary pickers this
 * keeps asking the server as the term changes. The debounce lives in `useExperimentSuggestions` —
 * it gates `enabled` rather than lagging the term, which leaves `isPending` as one signal spanning
 * both the wait and the request.
 */
export function ExperimentRefsCombobox({
  experimentId,
  value,
  onValueChange,
  id,
  disabled,
}: {
  /** The experiment being edited. It is dropped from the suggestions — see below. */
  experimentId: string;
  value: ExperimentRef[];
  onValueChange: (value: ExperimentRef[]) => void;
  id: string;
  disabled?: boolean;
}) {
  const [inputValue, setInputValue] = useState('');
  const query = inputValue.trim();
  const { data, isPending, isError } = useExperimentSuggestions(query);

  const chosen = new Set(value.map(refKey));
  /*
   * Two things are dropped, because offering either is offering a no-op or a mistake: refs
   * already held here, and the experiment being edited — nothing links to, continues to or
   * continues from itself. The backend's `suggest` is a plain name prefix match and excludes
   * neither, so the filtering is ours.
   *
   * This hides them from the picker only. A self-reference already stored still renders as a chip
   * and can still be removed, which is what you want from a filter meant to prevent new mistakes
   * rather than to conceal old ones.
   */
  const suggestions = (data ?? []).filter((ref) => !chosen.has(refKey(ref)) && ref.id !== experimentId);

  return (
    <MultiCombobox<ExperimentRef>
      id={id}
      value={value}
      onValueChange={onValueChange}
      items={suggestions}
      itemToKey={refKey}
      itemToLabel={(ref) => ref.name}
      inputValue={inputValue}
      onInputValueChange={setInputValue}
      placeholder="Type to search..."
      emptyMessage="No matching experiments"
      // An empty box is not pending anything; the query is disabled until something is typed.
      loading={query !== '' && isPending}
      // apiFetch has already toasted the failure; this says why the list is empty.
      error={isError}
      disabled={disabled}
    />
  );
}
