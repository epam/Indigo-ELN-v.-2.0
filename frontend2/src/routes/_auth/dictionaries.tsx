import { createFileRoute } from '@tanstack/react-router';

import { RequirePermission } from '@/components/auth/require-permission';
import { DictionaryList } from '@/components/dictionaries/dictionary-list';
import { DictionarySheet } from '@/components/dictionaries/dictionary-sheet';
import { useDictionaries } from '@/lib/api/dictionaries';
import { z } from '@/lib/zod';

/** The open dictionary's id, so the sheet is linkable and Back closes it. */
const searchSchema = z.object({
  dictionary: z.string().optional(),
});

export const Route = createFileRoute('/_auth/dictionaries')({
  validateSearch: searchSchema,
  head: () => ({ meta: [{ title: 'Indigo ELN - Dictionaries' }] }),
  component: () => (
    <RequirePermission permission="MANAGE_DICTIONARIES">
      <DictionariesPage />
    </RequirePermission>
  ),
});

function DictionariesPage() {
  const { dictionary: selectedId } = Route.useSearch();
  const navigate = Route.useNavigate();

  const query = useDictionaries();
  // Resolved from the list the page already has, rather than fetched again. A `?dictionary=`
  // naming nothing — a stale link, or a dictionary since deleted — leaves the sheet shut.
  const selected = query.data?.find((item) => item.id === selectedId);

  // Pushed, not replaced: opening and closing a sheet are two discrete gestures, so Back should
  // undo them. (`ActionBar` replaces because it writes on every keystroke.) resetScroll stays off
  // — the list under the sheet is unchanged and the reader's place in it should be too.
  const select = (id: string | undefined) =>
    void navigate({ search: (prev) => ({ ...prev, dictionary: id }), resetScroll: false });

  return (
    <>
      <h1 className="text-[16px]/6 font-semibold">Dictionaries</h1>
      <DictionaryList query={query} selectedId={selected?.id} onSelect={(item) => select(item.id)} />
      <DictionarySheet dictionary={selected} onClose={() => select(undefined)} />
    </>
  );
}
