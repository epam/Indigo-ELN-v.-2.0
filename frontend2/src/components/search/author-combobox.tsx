import { useState } from 'react';

import { Button } from '@/components/ui/button';
import { MultiCombobox } from '@/components/ui/combobox';
import { useCurrentUser, useUserSuggestions } from '@/lib/api/user';

import type { UserRef } from '@/lib/types/common.ts';

function userKey(user: UserRef): string {
  return user.username;
}

/**
 * Authors to filter by, with server-side suggestions and a shortcut for the signed-in user.
 *
 * `users/suggest` matches prefixes and caps at ten, so unlike the dictionary pickers this
 * one has to keep asking the server as the term changes. The debounce lives in
 * `useUserSuggestions` — it gates `enabled` rather than lagging the term, which leaves
 * `isPending` as one signal spanning both the wait and the request.
 */
function AuthorCombobox({
  value,
  onValueChange,
  id,
}: {
  value: UserRef[];
  onValueChange: (value: UserRef[]) => void;
  id: string;
}) {
  const [inputValue, setInputValue] = useState('');
  const query = inputValue.trim();
  const { data, isPending, isError } = useUserSuggestions(query);
  const { data: currentUser } = useCurrentUser();

  const chosen = new Set(value.map(userKey));
  // Already-chosen authors would be no-ops in the list, so drop them.
  const suggestions = (data ?? []).filter((user) => !chosen.has(userKey(user)));

  function addMe() {
    if (!currentUser || chosen.has(currentUser.username)) return;
    onValueChange([...value, { username: currentUser.username, displayName: currentUser.displayName }]);
  }

  return (
    <div className="flex flex-col items-start gap-1">
      <MultiCombobox<UserRef>
        id={id}
        value={value}
        onValueChange={onValueChange}
        items={suggestions}
        itemToKey={userKey}
        itemToLabel={(user) => user.displayName}
        inputValue={inputValue}
        onInputValueChange={setInputValue}
        placeholder="Type to search..."
        emptyMessage="No matching users"
        // An empty box is not pending anything; the query is disabled until something is typed.
        loading={query !== '' && isPending}
        // apiFetch has already toasted the failure; this says why the list is empty.
        error={isError}
      />
      <Button
        type="button"
        variant="link"
        size="sm"
        className="px-0 underline"
        // Nothing to add until currentUser resolves, and nothing to add twice.
        disabled={!currentUser || chosen.has(currentUser.username)}
        onClick={addMe}
      >
        Add Me as an Author
      </Button>
    </div>
  );
}

export { AuthorCombobox };
