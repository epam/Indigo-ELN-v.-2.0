import type { DateString, UserRef } from '@/lib/types/common.ts';

/**
 * Mirrors the backend's `RevisionSummaryDTO`, which the project, notebook and experiment
 * revision endpoints all answer with. Only the experiment's is read today.
 *
 * The DTO is `@JsonInclude(NON_NULL)`, so the last three fields are **absent** rather than null,
 * and their presence is the whole signal: an entry carrying them is a grouped edit session —
 * consecutive revisions by one user that the backend collapsed into a single row spanning
 * `revision`…`revisionTo` and `date`…`dateTo`, with the individual revisions in `details`.
 */
export interface RevisionSummary {
  user: UserRef;
  summary: string;
  date: DateString;
  /** Set only on a grouped edit session: when the last revision in the group was made. */
  dateTo?: DateString;
  revision: number;
  /** Set only on a grouped edit session: the last revision number in the group. */
  revisionTo?: number;
  /** Set only on a grouped edit session: the revisions it collapsed, in the same order. */
  details?: RevisionSummary[];
}
