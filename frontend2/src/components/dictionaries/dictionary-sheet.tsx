import { DictionaryWordsTable } from '@/components/dictionaries/dictionary-words-table';
import { Dialog, DialogContent } from '@/components/ui/dialog';

import type { Dictionary } from '@/lib/types/dictionaries.ts';

function Section({ label, children }: { label: string; children: string }) {
  return (
    <div className="flex flex-col gap-2">
      <h3 className="font-semibold text-neutral-800">{label}</h3>
      <p className="text-neutral-1000">{children}</p>
    </div>
  );
}

/**
 * One dictionary, opened over the list it was clicked in — `DialogContent side="right"`, the same
 * sheet Global Search and the experiment's Team panel use, which is why there is no `Sheet`
 * component in `ui/`. The transparent backdrop is what makes the list behind it stay legible, as
 * indigo-frontend's `hasBackdrop="false"` drawer does.
 *
 * The About card is read-only. `PATCH /dictionaries/{ref}` would let it rename the dictionary and
 * rewrite its description, but neither is part of this screen.
 *
 * Mounted by the route unconditionally with `open` driving it: a sheet behind a `{open && …}`
 * flag cannot animate out, because React removes the element before Base UI can transition it.
 * Base UI unmounts the popup itself once the exit finishes, so nothing inside — including
 * `useDictionaryItems` — keeps running while the sheet is shut.
 */
export function DictionarySheet({ dictionary, onClose }: { dictionary: Dictionary | undefined; onClose: () => void }) {
  return (
    <Dialog open={dictionary !== undefined} onOpenChange={(open) => !open && onClose()}>
      <DialogContent title="Dictionary" side="right">
        {dictionary && (
          <>
            <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
              <h2 className="border-b border-neutral-300 pb-3 text-[16px]/6 font-semibold">About Dictionary</h2>
              <div className="flex flex-col gap-6 text-[14px]/6">
                <Section label="Dictionary Name">{dictionary.name}</Section>
                <Section label="Description">{dictionary.description || '—'}</Section>
              </div>
            </section>

            <DictionaryWordsTable dictionary={dictionary} />
          </>
        )}
      </DialogContent>
    </Dialog>
  );
}
