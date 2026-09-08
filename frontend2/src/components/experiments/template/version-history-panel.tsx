import { ChevronDown, ChevronRight, FileDiff } from 'lucide-react';
import type { ReactNode } from 'react';
import { useState } from 'react';

import { CELL_CLASS, HEADER_CELL_CLASS } from '@/components/experiments/stoichiometry/columns';
import { hasDiff, isGroup, revisionDateLabel, revisionLabel } from '@/components/experiments/template/version-history';
import { Skeleton } from '@/components/ui/skeleton';
import { useExperimentRevisions, useRevisionDiff } from '@/lib/api/experiments';
import { describeError } from '@/lib/toast';
import { cn } from '@/lib/utils';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { RevisionSummary } from '@/lib/types/revisions.ts';

/** Chevron, #, Date, User, Summary, diff button — what a diff row has to span. */
const COLUMN_COUNT = 6;

/**
 * The `versionHistory` template component — the experiment's revision log. Uncollapsible, for the
 * reason given in `BatchesPanel`.
 *
 * Ported from indigo-frontend's `AuditLogComponent`, which was written entity-agnostic for the
 * project and notebook revision endpoints and then only ever used by the experiment. This is the
 * experiment's, directly: nothing renders the other two, and only the experiment has a diff
 * endpoint at all.
 */
export function VersionHistoryPanel({ experiment }: { experiment: ExperimentDetails }) {
  const { data: revisions, isPending, error } = useExperimentRevisions(experiment.id, experiment.revision);

  return (
    <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
      <h2 className="text-[16px]/6 font-semibold text-neutral-1000">Version History</h2>

      <div className="overflow-x-auto rounded-6 border border-neutral-300 bg-card">
        <table className="w-full border-collapse">
          <caption className="sr-only">Revision log, newest first</caption>
          <thead>
            <tr>
              {/* The chevron and diff columns speak for themselves. */}
              <th className={cn(HEADER_CELL_CLASS, 'w-11')} />
              {/* The tree column: a child of a group is indented in it, so it aligns left. */}
              <th scope="col" className={cn(HEADER_CELL_CLASS, 'w-24 text-left')}>
                #
              </th>
              <th scope="col" className={cn(HEADER_CELL_CLASS, 'text-left')}>
                Date
              </th>
              <th scope="col" className={cn(HEADER_CELL_CLASS, 'text-left')}>
                User
              </th>
              <th scope="col" className={cn(HEADER_CELL_CLASS, 'text-left')}>
                Summary
              </th>
              <th className={cn(HEADER_CELL_CLASS, 'w-11')} />
            </tr>
          </thead>

          {isPending ? (
            <tbody aria-busy="true">
              {Array.from({ length: 4 }, (_, index) => (
                <tr key={index}>
                  <td className={CELL_CLASS} colSpan={COLUMN_COUNT}>
                    <Skeleton className="h-5 w-full" />
                  </td>
                </tr>
              ))}
            </tbody>
          ) : (
            revisions?.map((revision) => (
              <RevisionRows key={revision.revision} experimentId={experiment.id} revision={revision} />
            ))
          )}
        </table>

        {isPending && (
          // The skeleton rows are decoration; this is what a screen reader is told, and it lives
          // outside the table so it is not a row that is not a row.
          <p role="status" className="sr-only">
            Loading the revision log…
          </p>
        )}
        {error != null && (
          // Repeated from the toast apiFetch already raised: a toast is gone in five seconds and
          // an empty table is not.
          <p className="p-6 text-center text-[14px]/6 text-destructive">
            Could not load the revision log: {describeError(error)[0]}
          </p>
        )}
        {!isPending && error == null && revisions?.length === 0 && (
          // Not reachable in practice — creating an experiment is revision 1 — but an empty box
          // with no explanation is the worse failure if it ever is.
          <p className="p-6 text-center text-[14px]/6 text-neutral-700">No revisions recorded.</p>
        )}
      </div>
    </section>
  );
}

/**
 * One top-level entry: the row itself, its diff when open, and — for a grouped edit session —
 * its children and theirs.
 *
 * A `<tbody>` per entry, so a row and everything belonging to it stay together, the same shape
 * `SampleResults` uses.
 *
 * Both open sets are keyed by **revision number**, which is unique within an experiment.
 * indigo-frontend keys its diff map by the revision *object*, which silently loses every open
 * diff the moment the list is refetched.
 */
function RevisionRows({ experimentId, revision }: { experimentId: string; revision: RevisionSummary }) {
  const [expanded, setExpanded] = useState(false);
  const [openDiffs, setOpenDiffs] = useState<ReadonlySet<number>>(() => new Set());

  function toggleDiff(revisionNo: number) {
    setOpenDiffs((open) => {
      const next = new Set(open);
      if (!next.delete(revisionNo)) next.add(revisionNo);
      return next;
    });
  }

  const group = isGroup(revision);

  return (
    <tbody>
      <RevisionRow
        experimentId={experimentId}
        revision={revision}
        diffOpen={openDiffs.has(revision.revision)}
        onToggleDiff={() => toggleDiff(revision.revision)}
        chevron={
          group && (
            <button
              type="button"
              onClick={() => setExpanded((open) => !open)}
              aria-expanded={expanded}
              aria-label={`${expanded ? 'Hide' : 'Show'} the revisions in ${revision.summary} ${revisionLabel(revision)}`}
              className="rounded-2 p-1 text-neutral-700 outline-none hover:text-neutral-1000 focus-visible:ring-3 focus-visible:ring-ring/50"
            >
              {expanded ? <ChevronDown className="size-4" /> : <ChevronRight className="size-4" />}
            </button>
          )
        }
      />

      {group &&
        expanded &&
        revision.details?.map((child) => (
          <RevisionRow
            key={child.revision}
            experimentId={experimentId}
            revision={child}
            nested
            diffOpen={openDiffs.has(child.revision)}
            onToggleDiff={() => toggleDiff(child.revision)}
          />
        ))}
    </tbody>
  );
}

/** One revision, and the diff row beneath it while that diff is open. */
function RevisionRow({
  experimentId,
  revision,
  nested = false,
  chevron,
  diffOpen,
  onToggleDiff,
}: {
  experimentId: string;
  revision: RevisionSummary;
  /** A child of a grouped edit session: tinted, and marked as belonging to the row above. */
  nested?: boolean;
  chevron?: ReactNode;
  diffOpen: boolean;
  onToggleDiff: () => void;
}) {
  const label = `${diffOpen ? 'Hide' : 'Show'} the changes in revision ${revisionLabel(revision)}`;

  /*
   * The button and the diff row are gated on the *same* predicate, which is what stops a group
   * header from mirroring its oldest child's diff.
   *
   * `revisionGroupToSummary` builds a group from `list.getFirst()`, so a group's `revision` is
   * its oldest child's — the two are one key in `openDiffs`, and after the newest-first reversal
   * that child is the one shown last. Opening its diff therefore also flips the header's
   * `diffOpen`, and gating the row on `diffOpen` alone put a second copy directly under the
   * header, above the whole group. `hasDiff` is false for a group, so asking it here is enough;
   * de-duplicating the key would mean inventing one, since the collision is in the data.
   */
  const canDiff = hasDiff(revision);

  return (
    <>
      <tr className={cn('hover:bg-neutral-100', nested && 'bg-neutral-100/60')}>
        {/*
          The chevron column is empty on a child, so it is what carries the nesting: an accent bar
          down the left of every revision in the group, which is legible where a tint alone is not
          once the group runs past the fold.
        */}
        <td className={cn(CELL_CLASS, nested && 'border-l-2 border-l-blue-100')}>{chevron}</td>
        {/*
          The indent that says this row belongs to the one above it, on the two columns that open
          the line. It stops there rather than running across the whole row: User and Summary are
          what the log is read down, and shifting those would break the column the eye follows —
          while indenting `#` alone (a number two characters wide) is too small a step to notice.
        */}
        <td className={cn(CELL_CLASS, 'text-[13px]/5 tabular-nums text-neutral-700', nested && 'pl-9')}>
          {revisionLabel(revision)}
        </td>
        <td className={cn(CELL_CLASS, 'text-[13px]/5 whitespace-nowrap text-neutral-800', nested && 'pl-9')}>
          {revisionDateLabel(revision)}
        </td>
        <td className={cn(CELL_CLASS, 'text-[13px]/5 text-neutral-800')}>{revision.user.displayName}</td>
        <td className={cn(CELL_CLASS, 'text-[13px]/5 text-neutral-1000')}>{revision.summary}</td>
        <td className={CELL_CLASS}>
          {canDiff && (
            <button
              type="button"
              onClick={onToggleDiff}
              aria-pressed={diffOpen}
              aria-label={label}
              title={label}
              className={cn(
                'rounded-2 p-1 outline-none hover:bg-blue-10 focus-visible:ring-3 focus-visible:ring-ring/50',
                diffOpen ? 'bg-blue-10 text-blue-400' : 'text-neutral-700',
              )}
            >
              <FileDiff className="size-4" />
            </button>
          )}
        </td>
      </tr>

      {canDiff && diffOpen && (
        <tr>
          <td className={cn(CELL_CLASS, 'px-6 py-3', nested && 'border-l-2 border-l-blue-100')} colSpan={COLUMN_COUNT}>
            <RevisionDiff experimentId={experimentId} revisionNo={revision.revision} />
          </td>
        </tr>
      )}
    </>
  );
}

/**
 * One revision's diff, mounted only while it is open — which is the whole gating: building one
 * rewinds the experiment snapshot server-side and renders an SVG through Indigo per structure
 * in it, so nothing should ask for a diff nobody is looking at.
 */
function RevisionDiff({ experimentId, revisionNo }: { experimentId: string; revisionNo: number }) {
  const { data, isPending, error } = useRevisionDiff(experimentId, revisionNo);

  if (isPending) {
    return (
      <p role="status" className="text-[13px]/5 text-neutral-700">
        Loading the changes…
      </p>
    );
  }
  if (error != null) {
    return <p className="text-[13px]/5 text-destructive">Could not load the changes: {describeError(error)[0]}</p>;
  }

  // The HTML is built by `PatchFormatter` on our own backend out of stored model values — the
  // same trust boundary every other field on this page sits behind, and the same reasoning
  // indigo-frontend's `bypassSecurityTrustHtml` carries. `.patch-diff` in `styles.css` is what
  // styles it: the fragment arrives with bare classes and no stylesheet of its own.
  return <div className="patch-diff overflow-x-auto" dangerouslySetInnerHTML={{ __html: data }} />;
}
