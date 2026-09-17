import { useState } from 'react';

import { MultiCombobox } from '@/components/ui/combobox';
import { useKeywordSuggestions } from '@/lib/api/projects';

/**
 * Project keywords: free-form strings with server-side suggestions.
 *
 * The typed term goes straight into the query key; `useKeywordSuggestions` owns the
 * debounce by gating its own `enabled`, which leaves `isPending` as one signal covering
 * both the wait and the request. Lagging the term here instead would leave a window where
 * the popup claimed "no matches" before anything had been asked.
 */
function KeywordCombobox({
  value,
  onValueChange,
  id,
}: {
  value: string[];
  onValueChange: (value: string[]) => void;
  id?: string;
}) {
  const [inputValue, setInputValue] = useState('');
  const query = inputValue.trim();
  const { data, isPending, isError } = useKeywordSuggestions(query);

  // Already-chosen keywords would be no-ops in the list, so drop them.
  const suggestions = (data ?? []).filter((keyword) => !value.includes(keyword));

  return (
    <MultiCombobox
      id={id}
      value={value}
      onValueChange={onValueChange}
      items={suggestions}
      inputValue={inputValue}
      onInputValueChange={setInputValue}
      placeholder="Add Keyword"
      emptyMessage="No matching keywords"
      // Project keywords are arbitrary strings — ProjectMutation.CreateProject accepts any
      // List<String> — so anything typed is a valid keyword.
      allowCustomValues
      // An empty box is not pending anything; the query is disabled until something is typed.
      loading={query !== '' && isPending}
      // apiFetch has already toasted the failure; this says why the list is empty.
      error={isError}
    />
  );
}

export { KeywordCombobox };
