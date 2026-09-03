import { Bookmark, ChevronDown, ChevronRight, Plus } from 'lucide-react';
import { useEffect, useMemo, useRef, useState } from 'react';

import { ApiImage } from '@/components/common/api-image';
import type { ResolveInputMutations } from '@/components/experiments/analyze-rxn/use-resolve-input';
import { sampleRowKey } from '@/components/experiments/analyze-rxn/use-resolve-input';
import { resultCountLabel } from '@/components/experiments/analyze-rxn/result-count';
import { EmptyCell, FormulaCell, IconActionCell, ReadonlyCell } from '@/components/experiments/stoichiometry/cells';
import { CELL_CLASS, HEADER_CELL_CLASS } from '@/components/experiments/stoichiometry/columns';
import { Skeleton } from '@/components/ui/skeleton';
import { samplePicturePath, useMarkSample, useSampleSearch } from '@/lib/api/samples';
import { describeError } from '@/lib/toast';
import { cn } from '@/lib/utils';

import type { UUID } from '@/lib/types/common.ts';
import type { SampleCatalogFilter, SampleDTO } from '@/lib/types/samples.ts';
import { CATALOGS_BY_FILTER } from '@/lib/types/samples.ts';

/** Chevron + the four data columns + the two action columns. */
const COLUMN_COUNT = 7;

const DATA_COLUMNS = ['Compound ID', 'Chemical Name', 'Mol. Weight', 'Mol. Formula'];

/**
 * One tab's results: everything the catalogs offer for one unresolved reactant.
 *
 * The search runs on mount and needs no gesture — the molfile of the row that could not be
 * matched is the whole query, so there is nothing for the user to type. Because Base UI unmounts
 * an inactive tab panel, "on mount" also means *when the tab is first opened*: a step with five
 * unresolved reactants does not fire five substructure searches (and five PubChem round trips)
 * at once, the way indigo-frontend's `ngOnInit` does. What has been searched stays in cache, so
 * coming back to a tab is instant.
 *
 * Switching the catalog does not go through here at all: `catalog` is part of the request, the
 * request is the query key, and a new key is a new query.
 */
export function SampleResults({
  molfile,
  catalog,
  inputAnchor,
  resolve,
  boundSamples,
  onCountChange,
}: {
  /** The unmatched structure, as `unresolvedInputs` gave it. */
  molfile: string;
  catalog: SampleCatalogFilter;
  inputAnchor: UUID;
  resolve: ResolveInputMutations;
  /** Sample ids the step already holds — those rows cannot be added again. */
  boundSamples: ReadonlySet<UUID>;
  /** Reports this tab's result count up to its label, already worded. Null while unknown. */
  onCountChange: (inputAnchor: UUID, count: string | null) => void;
}) {
  const [expanded, setExpanded] = useState<ReadonlySet<string>>(() => new Set());

  const request = useMemo(
    () => ({
      catalogs: CATALOGS_BY_FILTER[catalog],
      // The backend matches with Bingo's `bingo_substructure_match`; an exact search would miss
      // every registered salt and solvate of the thing that was drawn.
      structure: { type: 'SUBSTRUCTURE' as const, query: molfile },
    }),
    [catalog, molfile],
  );

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
   * The count is a property of the search, but it is shown on the tab, which outlives this
   * panel — so it is worded here and reported upward rather than rendered here.
   *
   * `totalItems` is read off the **last** page, not the first: it is a running total, and the
   * page that has seen the most catalogs is the one with the most to say. It is null from the
   * moment a catalog that cannot count has contributed, and `resultCountLabel` is what turns
   * that into `12+` — or into a plain `12` once the cursor is spent.
   */
  const totalItems = data?.pages[data.pages.length - 1]?.totalItems ?? null;
  const countLabel = resultCountLabel({ totalItems, loaded: samples.length, hasMore: hasNextPage, loading: isPending });
  useEffect(() => {
    onCountChange(inputAnchor, countLabel);
  }, [onCountChange, inputAnchor, countLabel]);

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
          <col className="w-[150px]" />
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
                adding={resolve.addingRows.has(row)}
                alreadyBound={sample.id != null && boundSamples.has(sample.id)}
                onAdd={() => resolve.add(inputAnchor, sample)}
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
        <p className="p-6 text-center text-[14px]/6 text-neutral-700">No materials match this structure.</p>
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
  const name = sample.name ?? sample.compoundKey ?? sample.molFormula;

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
          alt={`Structure of ${sample.name ?? sample.compoundKey ?? sample.molFormula}`}
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
