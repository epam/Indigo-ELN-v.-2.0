import { createFileRoute } from '@tanstack/react-router';

import { RequirePermission } from '@/components/auth/require-permission';
import { ActionBar } from '@/components/common/action-bar';
import { SignatureCollection } from '@/components/signatures/signature-collection';
import { z } from '@/lib/zod';
import { SORT_ORDERS, type SortOrder } from '@/lib/types/common.ts';

/**
 * No `view`: the signatures list has one layout, so the bar shows no toggle and there is nothing
 * to remember. `LATEST` matches the backend's own default ordering on `lastModifiedDate`.
 */
const searchSchema = z.object({
  q: z.string().optional(),
  sort: z.enum(SORT_ORDERS).default('LATEST'),
  waitingMySignature: z.boolean().default(false),
});

export const Route = createFileRoute('/_auth/signatures')({
  validateSearch: searchSchema,
  head: () => ({ meta: [{ title: 'Indigo ELN - Signatures' }] }),
  component: () => (
    <RequirePermission permission="SIGN_EXPERIMENTS">
      <SignaturesPage />
    </RequirePermission>
  ),
});

function SignaturesPage() {
  const { q, sort, waitingMySignature } = Route.useSearch();
  const navigate = Route.useNavigate();

  // replace: keystrokes and toggles should not each become a history entry.
  const patch = (next: Partial<z.infer<typeof searchSchema>>) =>
    void navigate({ search: (prev) => ({ ...prev, ...next }), replace: true });

  return (
    <>
      <h1 className="text-[16px]/6 font-semibold">Signatures</h1>
      <ActionBar
        entityLabel="signatures"
        search={q ?? ''}
        sort={sort}
        // ActionBar's switch is labelled "My Entities" everywhere; on this endpoint the param
        // behind it is `waitingMySignature`, which is looser than its name — the repository
        // filters to documents the user is a signer on, whatever the state of their block.
        createdByMe={waitingMySignature}
        onSearchChange={(value) => patch({ q: value || undefined })}
        onSortChange={(value: SortOrder) => patch({ sort: value })}
        onCreatedByMeChange={(value) => patch({ waitingMySignature: value })}
      />
      <SignatureCollection filters={{ search: q ?? '', sort, waitingMySignature }} />
    </>
  );
}
