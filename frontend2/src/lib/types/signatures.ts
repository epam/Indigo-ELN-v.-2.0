import type { DateString, DocumentStatus, SortOrder, UserRef, UUID } from '@/lib/types/common.ts';

/**
 * Mirrors the signature service's DTOs (`signature/signature-api/.../model/`), which the ELN
 * ones do not share a base with: `DocumentDTO` does not extend `BaseDTO`, so its timestamps are
 * `createdDate`/`lastModifiedDate` rather than `createdAt`/`modifiedAt`, and there is no
 * `modifiedBy` to name beside them.
 */

/** Why a block is on the document — `SignatureReason`. Lower case: it is read inline, as "admin (author)". */
export type SignatureReason = 'AUTHOR' | 'WITNESS';

export const SIGNATURE_REASON_LABELS: Record<SignatureReason, string> = {
  AUTHOR: 'author',
  WITNESS: 'witness',
};

/**
 * `SignatureStatus` — where one signer stands. `WAITING` reads as "Pending" because that is what
 * it means to everyone but the signer themselves, who is offered Approve/Reject instead.
 */
export type SignatureStatus = 'WAITING' | 'APPROVED' | 'REJECTED';

export const SIGNATURE_STATUS_LABELS: Record<SignatureStatus, string> = {
  WAITING: 'Pending',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
};

/** The same tokens `EXPERIMENT_STATUS_COLOR` uses for the statuses of these names. */
export const SIGNATURE_STATUS_COLOR: Record<SignatureStatus, string> = {
  WAITING: 'text-neutral-700',
  APPROVED: 'text-green-200',
  REJECTED: 'text-red-200',
};

export interface DocumentSignature {
  id: UUID;
  user: UserRef;
  reason: SignatureReason;
  /** Absent until the signer acts. */
  actionDate?: DateString;
  status: SignatureStatus;
  comment?: string;
  /**
   * Computed per request by `SignatureMapper.canSignOrReject`: the document is SUBMITTED or
   * SIGNING, this block belongs to the caller, and it is still WAITING. The only gate on the
   * Approve/Reject pair — the client re-derives none of it.
   */
  canSignOrReject: boolean;
}

/**
 * One submitted experiment version awaiting signature.
 *
 * Named `SignatureDocument` rather than `Document`, which would shadow the DOM global.
 *
 * `name` is `"<experiment name>, version <n>"`, composed by `SubmitExperimentHandler` when the
 * PDF is uploaded. It is the only experiment identity the row carries: nothing here resolves back
 * to an experiment id, and no endpoint offers that lookup.
 */
export interface SignatureDocument {
  id: UUID;
  name: string;
  status: DocumentStatus;
  createdDate: DateString;
  lastModifiedDate: DateString;
  author: UserRef;
  /** What the downloaded PDF is called, and the fallback when the response carries no filename. */
  filename: string;
  signatures: DocumentSignature[];
}

/**
 * Deliberately not `CollectionFilters`: `GET /documents` takes `waitingMySignature` where the ELN
 * collections take `createdByMe`, so `collectionQueryParams` cannot build this one.
 *
 * `waitingMySignature` is the backend's name and is looser than it sounds — the repository filters
 * to documents the user is *a signer on*, whatever the state of their block.
 */
export interface SignatureFilters {
  search: string;
  sort: SortOrder;
  waitingMySignature: boolean;
}
