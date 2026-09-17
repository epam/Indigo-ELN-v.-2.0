import { formatDateTime } from '@/lib/utils';

import type { RevisionSummary } from '@/lib/types/revisions.ts';

/** Whether this entry is a grouped edit session, i.e. whether it has children to expand. */
export function isGroup(revision: RevisionSummary): boolean {
  return (revision.details?.length ?? 0) > 0;
}

/**
 * The revision number as the `#` column shows it — `12`, or `8–12` for a grouped edit session.
 *
 * indigo-frontend shows neither: it reads `revision` only to decide whether a diff button
 * belongs on the row, so a log of "Edited experiment" rows has nothing to tell them apart.
 */
export function revisionLabel(revision: RevisionSummary): string {
  return revision.revisionTo == null ? String(revision.revision) : `${revision.revision}–${revision.revisionTo}`;
}

/**
 * When it happened — one instant, or the span a grouped edit session covers.
 *
 * `dateTo` and `revisionTo` are set together by `ExperimentMapper.revisionGroupToSummary`, but
 * this reads `dateTo` rather than inferring the span from `revisionTo`: the two answer different
 * columns, and a group of one is still a range of one instant to itself.
 */
export function revisionDateLabel(revision: RevisionSummary): string {
  const from = formatDateTime(revision.date);
  return revision.dateTo == null ? from : `${from} → ${formatDateTime(revision.dateTo)}`;
}

/**
 * Whether a row can show a diff. Revision 1 is the experiment being created and has nothing to
 * compare against — the endpoint refuses it with `@Min(2)` — and a group row has no single diff
 * of its own, only the ones its children carry.
 */
export function hasDiff(revision: RevisionSummary): boolean {
  return revision.revision > 1 && !isGroup(revision);
}
