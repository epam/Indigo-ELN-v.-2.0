import { Briefcase } from 'lucide-react';

import { LabelledColumn as Column } from '@/components/common/labelled-column';
import { Button } from '@/components/ui/button';

import { useDownloadDocument, useSignatureDecision } from '@/lib/api/signatures';
import type { DocumentSignature, SignatureDocument } from '@/lib/types/signatures.ts';
import { SIGNATURE_REASON_LABELS, SIGNATURE_STATUS_COLOR, SIGNATURE_STATUS_LABELS } from '@/lib/types/signatures.ts';
import { cn, formatDate } from '@/lib/utils.ts';

/**
 * Three explicit tracks rather than the `auto-fit` reflow the other rows use: the signatures cell
 * holds a line per signer and needs about twice a date's width, which equal tracks cannot give it.
 * Stacked below `md`, where three columns would leave nothing readable.
 */
const COLUMNS_CLASS = 'grid gap-4 grid-cols-1 md:grid-cols-[2fr_1fr_1fr]';

/**
 * One document awaiting signature: its name, who has to sign it and where each of them stands,
 * with Approve/Reject inline for the signer whose turn it is.
 *
 * An `<article>`, not a `<Link>` — `name` is `"<experiment name>, version <n>"` and there is no
 * endpoint that resolves it back to an experiment, so there is nowhere for a click to go. (The
 * Angular card is `cursor-pointer` with no handler; the cursor is dropped rather than copied.)
 */
export function SignatureRow({ item: document }: { item: SignatureDocument }) {
  const { downloadReport, downloading } = useDownloadDocument(document);

  return (
    <article className="flex flex-col gap-3 rounded-6 border border-neutral-300 bg-card px-4 pt-3 pb-4">
      <header className="flex h-7 items-center gap-4">
        <Briefcase className="size-5 shrink-0 text-neutral-800" />
        <h3 className="min-w-0 flex-1 truncate text-[14px]/5 font-semibold">{document.name}</h3>
        <Button variant="link" size="sm" loading={downloading} onClick={downloadReport}>
          Download Report
        </Button>
      </header>

      <dl className={COLUMNS_CLASS}>
        {/* Not LabelledColumn: its `dd` is a single flex row, and this one is a list. */}
        <div className="flex min-w-0 flex-col">
          <dt className="truncate text-[14px]/6 text-neutral-800">Signatures</dt>
          <dd className="flex flex-col gap-1 text-[14px]/6">
            {document.signatures.map((signature) => (
              <SignatureLine key={signature.id} documentId={document.id} signature={signature} />
            ))}
          </dd>
        </div>
        <Column label="Last Edited">
          <span className="truncate">{formatDate(document.lastModifiedDate)}</span>
        </Column>
        <Column label="Created">
          <span className="truncate">{formatDate(document.createdDate)}</span>
        </Column>
      </dl>
    </article>
  );
}

/**
 * One signer and their verdict.
 *
 * `canSignOrReject` is checked **before** the status, so the signer whose turn it is gets the two
 * buttons rather than the word "Pending" — the same branch order as the Angular template. The flag
 * is the backend's (`SignatureMapper.canSignOrReject`, which already knows the document status, the
 * current user and the block's own state), so nothing is re-derived here.
 *
 * The mutation lives per line rather than per row: two signers can be the same user only once, and
 * keeping it here is what lets exactly the pressed button spin.
 */
function SignatureLine({ documentId, signature }: { documentId: string; signature: DocumentSignature }) {
  const decide = useSignatureDecision(documentId);

  return (
    <div className="flex items-center gap-2">
      <span className="min-w-0 flex-1 truncate">
        {signature.user.displayName} ({SIGNATURE_REASON_LABELS[signature.reason]})
      </span>
      {signature.canSignOrReject ? (
        <span className="flex shrink-0 items-center gap-3">
          <Button
            variant="link"
            size="sm"
            disabled={decide.isPending}
            loading={decide.isPending && decide.variables === 'sign'}
            onClick={() => decide.mutate('sign')}
          >
            Approve
          </Button>
          <Button
            variant="link"
            size="sm"
            disabled={decide.isPending}
            loading={decide.isPending && decide.variables === 'reject'}
            onClick={() => decide.mutate('reject')}
          >
            Reject
          </Button>
        </span>
      ) : (
        <span className={cn('shrink-0', SIGNATURE_STATUS_COLOR[signature.status])}>
          {SIGNATURE_STATUS_LABELS[signature.status]}
        </span>
      )}
    </div>
  );
}
