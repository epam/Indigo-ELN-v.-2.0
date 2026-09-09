import { Bookmark, ChevronDown, ChevronRight, Plus } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';

import { ApiImage } from '@/components/common/api-image';
import { EmptyCell, FormulaCell, IconActionCell, ReadonlyCell } from '@/components/experiments/stoichiometry/cells';
import { CELL_CLASS, HEADER_CELL_CLASS } from '@/components/experiments/stoichiometry/columns';
import { Skeleton } from '@/components/ui/skeleton';
import { samplePicturePath, useMarkSample, useSampleSearch } from '@/lib/api/samples';
import { resultCountLabel, sampleRowKey } from '@/lib/search';
import { describeError } from '@/lib/toast';
import { cn } from '@/lib/utils';

import type { UUID } from '@/lib/types/common.ts';
import type { FindSamplesRequest, SampleDTO } from '@/lib/types/samples.ts';

/**
 * `molFormula` is HTML (`C<sub>9</sub>H<sub>8</sub>O<sub>4</sub>`), which is what `FormulaCell`
 * renders. An `aria-label` or an `alt` is plain text, though, so the tags would be read out
 * character by character — strip them wherever the formula stands in as the display name.
 */
const plainFormula = (molFormula: string) => molFormula.replace(/<[^>]+>/g, '');

/** Chevron + the four data columns + the two action columns. */
const COLUMN_COUNT = 7;

const DATA_COLUMNS = ['Compound ID', 'Chemical Name', 'Mol. Weight', 'Mol. Formula'];

/**
 * The catalog hits for one search, with a mark and an add on every row.
 *
 * Shared by the two surfaces that search `/samples/search`: Analyze RXN, which asks for one
 * substructure per unresolved reactant, and Add Material, which asks whatever its form was
 * filled in with. Neither the question nor what happens on Add lives here — the caller passes
 * the `request` and an `onAdd`, so this file only knows how to page a search and draw it.
 *
 * The request is the query key, so a caller that changes it (a different catalog, a new form
 * submission) starts a new query and leaves the old results in cache to come back instantly.
 * There is no gesture to wait for and nothing to debounce: whoever renders this has already
 * decided there is a search to run. In Analyze RXN "already decided" also means *when the tab is
 * first opened*, because Base UI unmounts an inactive tab panel — so a step with five unresolved
 * reactants does not fire five substructure searches (and five PubChem round trips) at once, the
 * way indigo-frontend's `ngOnInit` does.
 */
export function SampleResults({
  request,
  onAdd,
  addingRows,
  boundSamples,
  onCountChange,
  emptyMessage = 'No materials match this search.',
}: {
  /** What to search for. Must be stable across renders — it is the query key. */
  request: FindSamplesRequest;
  onAdd: (sample: SampleDTO) => void;
  /** Which rows are mid-add, keyed by `sampleRowKey`. */
  addingRows: ReadonlySet<string>;
  /** Sample ids the step already holds — those rows cannot be added again. */
  boundSamples: ReadonlySet<UUID>;
  /** Reports the result count, already worded. Null while unknown. Must be stable. */
  onCountChange?: (count: string | null) => void;
  /** What an empty result set says; the default suits a form, Analyze RXN names the structure. */
  emptyMessage?: string;
}) {
  const [expanded, setExpanded] = useState<ReadonlySet<string>>(() => new Set());

  const query = useSampleSearch(request);
  const { data, error, isPending, hasNextPage, isFetchingNextPage, fetchNextPage } = query;

  const sentinelRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel || !hasNextPage) return;

    const observer = new IntersectionObserver((entries) => {
      if (entries[0]?.isIntersecting && !isFetchingNextPage) void fetchNextPage();
    });
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  const samples = data?.pages.flatMap((page) => page.items) ?? [];

  /**
   * The count is a property of the search, but it is shown outside this box — on a tab, or above
   * the table — so it is worded here and reported upward rather than rendered here.
   *
   * `totalItems` is read off the **last** page, not the first: it is a running total, and the
   * page that has seen the most catalogs is the one with the most to say. It is null from the
   * moment a catalog that cannot count has contributed, and `resultCountLabel` is what turns
   * that into `12+` — or into a plain `12` once the cursor is spent.
   */
  const totalItems = data?.pages[data.pages.length - 1]?.totalItems ?? null;
  const countLabel = resultCountLabel({ totalItems, loaded: samples.length, hasMore: hasNextPage, loading: isPending });
  useEffect(() => {
    onCountChange?.(countLabel);
  }, [onCountChange, countLabel]);

  function toggle(row: string) {
    setExpanded((open) => {
      const next = new Set(open);
      if (!next.delete(row)) next.add(row);
      return next;
    });
  }

  return (
    // Vertical only: `table-fixed` below makes the table exactly as wide as this box, so there is
    // never anything to scroll sideways to.
    <div className="flex min-h-0 flex-1 flex-col overflow-y-auto rounded-6 border border-neutral-300 bg-card">
      {/*
        `table-fixed`, unlike the stoichiometry tables, which are auto-layout and scroll. Those
        live on the full page width; this one is in a sheet, and an auto layout there sizes every
        column to its content — so one long chemical name pushes the two action columns off the
        right edge, where they cannot be clicked without scrolling to them first.

        Fixed layout inverts that: the widths below are what the columns get, and Chemical Name —
        the only one left unsized — absorbs whatever is left over and truncates. So the actions
        are always in view, the two numeric columns keep the width their headers need, and the
        column that gives way is the one whose values are prose.
      */}
      <table className="w-full table-fixed border-collapse">
        <caption className="sr-only">Catalog search results</caption>
        {/*
          On the `<colgroup>` rather than on the cells: fixed layout takes its widths from here
          for the whole column, so the header and every row stay in step by construction.
        */}
        <colgroup>
          {/* Chevron, and the two icon buttons: an icon, its padding, and the cell's own. */}
          <col className="w-11" />
          <col className="w-37.5" />
          <col />
          <col className="w-24" />
          <col className="w-32" />
          <col className="w-11" />
          <col className="w-11" />
        </colgroup>
        <thead className="sticky top-0 z-10 bg-card">
          <tr>
            {/* The chevron and the two action columns speak for themselves. */}
            <th className={HEADER_CELL_CLASS} />
            {DATA_COLUMNS.map((header) => (
              // `truncate`: a fixed column is a hard edge, and a header wider than its own column
              // would otherwise spill across the next one instead of being clipped.
              <th key={header} scope="col" className={cn(HEADER_CELL_CLASS, 'truncate text-left')}>
                {header}
              </th>
            ))}
            <th className={HEADER_CELL_CLASS} />
            <th className={HEADER_CELL_CLASS} />
          </tr>
        </thead>

        {isPending ? (
          <tbody aria-busy="true">
            {Array.from({ length: 5 }, (_, index) => (
              <tr key={index}>
                <td className={CELL_CLASS} colSpan={COLUMN_COUNT}>
                  <Skeleton className="h-5 w-full" />
                </td>
              </tr>
            ))}
          </tbody>
        ) : (
          samples.map((sample) => {
            const row = sampleRowKey(sample);
            return (
              <SampleRow
                key={row}
                sample={sample}
                expanded={expanded.has(row)}
                onToggle={() => toggle(row)}
                adding={addingRows.has(row)}
                alreadyBound={sample.id != null && boundSamples.has(sample.id)}
                onAdd={() => onAdd(sample)}
              />
            );
          })
        )}
      </table>

      {isPending && (
        // The skeleton rows are decoration; this is what a screen reader is told, and it lives
        // outside the table so it is not a row that is not a row.
        <p role="status" className="sr-only">
          Searching the catalogs…
        </p>
      )}
      {error != null && (
        // Repeated from the toast apiFetch already raised: a toast is gone in five seconds and
        // an empty table is not, and this says which search came back with nothing.
        <p className="p-6 text-center text-[14px]/6 text-destructive">
          Could not search the catalogs: {describeError(error)[0]}
        </p>
      )}
      {!isPending && error == null && samples.length === 0 && (
        <p className="p-6 text-center text-[14px]/6 text-neutral-700">{emptyMessage}</p>
      )}

      {isFetchingNextPage && (
        <p className="p-3 text-center text-[13px]/5 text-neutral-700" role="status">
          Loading more…
        </p>
      )}
      <div ref={sentinelRef} aria-hidden className="h-px shrink-0" />
    </div>
  );
}

/** One hit and, when open, its detail — a `<tbody>`, so the two stay together. */
function SampleRow({
  sample,
  expanded,
  onToggle,
  adding,
  alreadyBound,
  onAdd,
}: {
  sample: SampleDTO;
  expanded: boolean;
  onToggle: () => void;
  adding: boolean;
  alreadyBound: boolean;
  onAdd: () => void;
}) {
  const markSample = useMarkSample();
  const name = sample.name ?? sample.compoundKey ?? plainFormula(sample.molFormula);

  return (
    <tbody>
      <tr className="hover:bg-neutral-100">
        <td className={CELL_CLASS}>
          <button
            type="button"
            onClick={onToggle}
            aria-expanded={expanded}
            aria-label={`${expanded ? 'Hide' : 'Show'} details of ${name}`}
            className="rounded-2 p-1 text-neutral-700 outline-none hover:text-neutral-1000 focus-visible:ring-3 focus-visible:ring-ring/50"
          >
            {expanded ? <ChevronDown className="size-4" /> : <ChevronRight className="size-4" />}
          </button>
        </td>
        <td className={CELL_CLASS}>
          <ReadonlyCell value={sample.compoundKey} />
        </td>
        <td className={CELL_CLASS}>
          <ReadonlyCell value={sample.name} />
        </td>
        <td className={CELL_CLASS}>
          <ReadonlyCell value={String(sample.molWeight)} />
        </td>
        <td className={CELL_CLASS}>
          {/* HTML, not text — `MolFormula` serialises through `@JsonValue toHTMLString()`. */}
          <FormulaCell value={sample.molFormula} />
        </td>
        <td className={CELL_CLASS}>
          {sample.id == null ? (
            // A catalog hit that is not an ELN sample yet cannot be marked: the list is a
            // per-user join on `Sample_Mark`, and there is no row to join to.
            <EmptyCell />
          ) : (
            <IconActionCell
              icon={sample.marked ? MarkedBookmark : Bookmark}
              tone="blue"
              label={sample.marked ? `Remove ${name} from My Materials` : `Add ${name} to My Materials`}
              editable={!markSample.isPending}
              pending={markSample.isPending}
              // `id` is checked above; narrowing does not survive into the closure.
              onCommit={() => markSample.mutate({ id: sample.id!, marked: !sample.marked })}
            />
          )}
        </td>
        <td className={CELL_CLASS}>
          <IconActionCell
            icon={Plus}
            tone="green"
            label={alreadyBound ? `${name} is already in the stoichiometry` : `Add ${name} to the stoichiometry`}
            editable={!alreadyBound}
            pending={adding}
            onCommit={onAdd}
          />
        </td>
      </tr>

      {expanded && (
        <tr>
          <td colSpan={COLUMN_COUNT} className="border-b border-neutral-300 bg-neutral-100 p-4">
            <SampleDetail sample={sample} />
          </td>
        </tr>
      )}
    </tbody>
  );
}

/** The filled bookmark. `IconActionCell` takes a component, so the fill has to be baked in. */
function MarkedBookmark({ className }: { className?: string }) {
  return <Bookmark className={cn('fill-current', className)} />;
}

/**
 * What one hit holds, behind its chevron: the record on the left and the structure on the right,
 * as in the design. Everything is read-only — a catalog result is somebody else's record, and the
 * only thing this dialog does to it is bind it to a row.
 */
function SampleDetail({ sample }: { sample: SampleDTO }) {
  const picture = samplePicturePath(sample);
  const frame = 'min-h-[220px] rounded-md border border-dashed border-neutral-300';

  return (
    <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,320px)]">
      <div className="grid gap-x-6 gap-y-4 md:grid-cols-2">
        <Fact label="Compound ID" value={sample.compoundKey} />
        <Fact label="Nbk Batch Number" value={sample.nbkBatchNumber} />
        <Fact label="Molecular Formula" html={sample.molFormula} />
        <Fact label="Molecular Weight" value={String(sample.molWeight)} />
        <Fact label="Chemical Name" value={sample.name} />
        <Fact label="Salt Code" value={sample.saltCode?.name} />
        <Fact label="Salt EQ" value={sample.saltEQ == null ? undefined : String(sample.saltEQ)} />
      </div>

      {picture == null ? (
        <div className={cn(frame, 'flex items-center justify-center p-4 text-center text-[14px]/6 text-neutral-700')}>
          No structure available
        </div>
      ) : (
        // `border-dashed` overrides ApiImage's own solid frame; the rest of its box is what we want.
        <ApiImage
          path={picture}
          alt={`Structure of ${sample.name ?? sample.compoundKey ?? plainFormula(sample.molFormula)}`}
          className={frame}
        />
      )}
    </div>
  );
}

/** A labelled read-only value, the same one the batch detail panel shows. */
function Fact({ label, value, html }: { label: string; value?: string; html?: string }) {
  return (
    <div className="flex flex-col gap-1">
      <h4 className="text-[14px]/6 font-semibold text-neutral-800">{label}</h4>
      {html != null && html !== '' ? (
        <p
          className="text-[14px]/6 text-neutral-1000 [&_sub]:align-sub [&_sub]:text-[0.75em] [&_sub]:leading-none"
          dangerouslySetInnerHTML={{ __html: html }}
        />
      ) : (
        <p className="text-[14px]/6 text-neutral-1000">{value || '—'}</p>
      )}
    </div>
  );
}
