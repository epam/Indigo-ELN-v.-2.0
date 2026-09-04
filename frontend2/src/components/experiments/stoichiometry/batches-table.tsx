import { ChevronDown, ChevronRight, CircleCheck, Download, Plus, RefreshCw, Search, Upload } from 'lucide-react';
import { useMemo, useRef, useState } from 'react';

import { SavingOverlay } from '@/components/common/saving-overlay';
import type { BatchColumn, BatchRow } from '@/components/experiments/stoichiometry/batch-columns';
import { BATCH_COLUMNS, batchHaystack, isSampleProtected } from '@/components/experiments/stoichiometry/batch-columns';
import { BatchDetailPanel } from '@/components/experiments/stoichiometry/batch-detail-panel';
import {
  DeleteCell,
  IconActionCell,
  OutputTypeBadge,
  ReadonlyCell,
} from '@/components/experiments/stoichiometry/cells';
import { CELL_CLASS, HEADER_CELL_CLASS } from '@/components/experiments/stoichiometry/columns';
import { NumericCell } from '@/components/experiments/stoichiometry/numeric-cell';
import type { StoichiometryMutations } from '@/components/experiments/stoichiometry/use-stoichiometry-mutations';
import { cellId, useStoichiometryMutations } from '@/components/experiments/stoichiometry/use-stoichiometry-mutations';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useExportSdf, useImportSdf } from '@/lib/api/experiments';
import { canEditExperiment } from '@/lib/types/experiments.ts';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { Reaction } from '@/lib/types/reactions.ts';

/**
 * The Product Batch Summary.
 *
 * One row per `ReactionOutputSample` — a *batch* — flattened across every output of the step. Two
 * things follow from that and neither is obvious:
 *
 * - **It does not filter on `intended`.** The products table shows only products drawn in the
 *   reaction scheme; a batch of something the reaction merely threw off has nowhere else to live,
 *   and the Sync with Products action is what promotes its product into that table.
 * - **Two anchors are in play.** Every column mutates the *sample*, except Sync, which mutates
 *   the *output* row. `BatchCell` picks the right one per kind; sending the wrong level 400s.
 *
 * Every edit is one mutation and there is no Save button, matching the rest of the screen.
 * Nothing is optimistic: a cell shows what the server last confirmed, and a failed save leaves
 * the draft alone (`apiFetch` has already raised the toast). The three editable numbers feed the
 * backend's calculator, so committing one weight comes back as a patch that also moves Total
 * Moles, Molarity and Yield — which is what the green flash on those cells is pointing out.
 */
export function ProductBatchSummaryTable({
  experiment,
  reaction,
  step,
}: {
  experiment: ExperimentDetails;
  reaction: Reaction;
  step: number;
}) {
  const [search, setSearch] = useState('');
  /**
   * Which rows are **open**. The opposite polarity to `StoichiometryTable`, which tracks the shut
   * ones because its nested batches are the point of that table and start expanded. A detail
   * panel is supporting material, so closed is the default — and tracking the positive is what
   * makes that the default for a batch that does not exist yet.
   */
  const [expanded, setExpanded] = useState<ReadonlySet<string>>(() => new Set());

  const mutations = useStoichiometryMutations(experiment);
  const canEdit = canEditExperiment(experiment);

  const batches = useMemo<BatchRow[]>(
    () => reaction.outputs.flatMap((output) => output.samples.map((sample) => ({ output, sample, step }))),
    [reaction.outputs, step],
  );

  const rows = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (term === '') return batches;
    return batches.filter((row) => batchHaystack(row).includes(term));
  }, [batches, search]);

  function toggle(anchor: string) {
    setExpanded((open) => {
      const next = new Set(open);
      if (!next.delete(anchor)) next.add(anchor);
      return next;
    });
  }

  return (
    <div className="flex flex-col gap-3">
      <Toolbar
        experiment={experiment}
        reaction={reaction}
        canEdit={canEdit}
        search={search}
        onSearchChange={setSearch}
        mutations={mutations}
      />

      <div className="overflow-x-auto rounded-6 border border-neutral-300 bg-card">
        {/* `w-full` is a preferred width, not a cap — see the note in `StoichiometryTable`. */}
        <table className="w-full border-collapse">
          <caption className="sr-only">Product batch summary</caption>
          <thead>
            <tr>
              {/* The chevron column has no header text; the chevrons speak for themselves. */}
              <th className={HEADER_CELL_CLASS} />
              {BATCH_COLUMNS.map((column) => (
                <th key={column.id} scope="col" className={HEADER_CELL_CLASS} style={{ minWidth: column.minWidth }}>
                  {column.header}
                </th>
              ))}
            </tr>
          </thead>
          {rows.map((row) => (
            <BatchRowGroup
              key={row.sample.anchor}
              row={row}
              reaction={reaction}
              expanded={expanded.has(row.sample.anchor)}
              onToggle={() => toggle(row.sample.anchor)}
              canEdit={canEdit}
              mutations={mutations}
            />
          ))}
        </table>

        {rows.length === 0 && (
          <p className="py-10 text-center text-[14px]/6 text-neutral-700">
            {batches.length === 0 ? 'No batch added' : 'No batch matches this search'}
          </p>
        )}
      </div>
    </div>
  );
}

function Toolbar({
  experiment,
  reaction,
  canEdit,
  search,
  onSearchChange,
  mutations,
}: {
  experiment: ExperimentDetails;
  reaction: Reaction;
  canEdit: boolean;
  search: string;
  onSearchChange: (search: string) => void;
  mutations: StoichiometryMutations;
}) {
  const fileInput = useRef<HTMLInputElement>(null);
  const addBatchCell = cellId(reaction.anchor, 'addBatch');
  const importSdf = useImportSdf(experiment, reaction.anchor);
  const { exportSdf, exporting } = useExportSdf(experiment.id);

  return (
    <div className="flex flex-wrap items-center justify-between gap-3">
      <div className="relative min-w-0 flex-1 sm:max-w-xs">
        <Search aria-hidden className="absolute top-1/2 left-3 size-4 -translate-y-1/2 text-neutral-700" />
        <Input
          type="search"
          aria-label="Search batches"
          placeholder="Search"
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
          className="pl-9"
        />
      </div>

      <div className="flex items-center gap-2">
        {/*
          `AddNoProductSample` appends a product row that was *not* drawn in the scheme —
          `intended: false`, `BY_PRODUCT`, on an unknown compound — plus one batch on it. That is
          why the new row lands here and not in the products table.

          indigo-frontend puts this behind a menu whose second entry adds a batch to an existing
          product. That entry is the per-row Add Batch button on the products table, so the menu
          would be one indirection over a single action.

          Wrapped in `SavingOverlay` rather than given `Button`'s own `loading`, as the
          stoichiometry table's Add empty row is: the flag goes through the 300 ms delay, so a
          fast write shows nothing instead of a spinner that blinks, and the button is inert
          meanwhile so the batch cannot be added twice. The two SDF buttons below keep `loading`
          — a file upload or download is never fast enough for the delay to matter.
        */}
        <SavingOverlay pending={mutations.savingCells.has(addBatchCell)} spinner="center">
          <Button
            variant="ghost"
            size="icon"
            aria-label="Add empty batch"
            title="Add empty batch"
            disabled={!canEdit}
            onClick={() => mutations.save(addBatchCell, { type: 'AddNoProductSample', anchor: reaction.anchor })}
          >
            <Plus />
          </Button>
        </SavingOverlay>

        <input
          ref={fileInput}
          type="file"
          accept=".sdf"
          className="hidden"
          onChange={(event) => {
            const file = event.target.files?.[0];
            // Cleared first, so picking the same file twice in a row still fires onChange.
            event.target.value = '';
            if (file) importSdf.mutate(file);
          }}
        />
        <Button
          variant="ghost"
          size="icon"
          aria-label="Import SDF"
          title="Import SDF"
          loading={importSdf.isPending}
          disabled={!canEdit}
          onClick={() => fileInput.current?.click()}
        >
          <Upload />
        </Button>

        {/* Readable without edit rights: exporting changes nothing. */}
        <Button
          variant="ghost"
          size="icon"
          aria-label="Export SDF"
          title="Export SDF"
          loading={exporting}
          onClick={exportSdf}
        >
          <Download />
        </Button>
      </div>
    </div>
  );
}

/**
 * One batch and, when open, its detail panel — a `<tbody>`, which is what keeps the two together
 * if anything ever reorders the rows.
 */
function BatchRowGroup({
  row,
  reaction,
  expanded,
  onToggle,
  canEdit,
  mutations,
}: {
  row: BatchRow;
  /** Only the detail panel needs it, for the reaction-level `precursorReactantIds`. */
  reaction: Reaction;
  expanded: boolean;
  onToggle: () => void;
  canEdit: boolean;
  mutations: StoichiometryMutations;
}) {
  const batch = row.sample.shortNbkBatchNumber;

  return (
    <tbody>
      <tr className="hover:bg-neutral-100">
        <td className={CELL_CLASS}>
          <button
            type="button"
            onClick={onToggle}
            aria-expanded={expanded}
            aria-label={`${expanded ? 'Hide' : 'Show'} details of batch ${batch}`}
            className="rounded-2 p-1 text-neutral-700 outline-none hover:text-neutral-1000 focus-visible:ring-3 focus-visible:ring-ring/50"
          >
            {expanded ? <ChevronDown className="size-4" /> : <ChevronRight className="size-4" />}
          </button>
        </td>
        {BATCH_COLUMNS.map((column) => (
          <td key={column.id} className={CELL_CLASS}>
            <BatchCell column={column} row={row} canEdit={canEdit} mutations={mutations} />
          </td>
        ))}
      </tr>

      {expanded && (
        <tr>
          {/* +1 for the chevron column, which is not in `BATCH_COLUMNS`. */}
          <td colSpan={BATCH_COLUMNS.length + 1} className="border-b border-neutral-300 bg-neutral-100 p-4">
            <BatchDetailPanel row={row} reaction={reaction} canEdit={canEdit} mutations={mutations} />
          </td>
        </tr>
      )}
    </tbody>
  );
}

function BatchCell({
  column,
  row,
  canEdit,
  mutations,
}: {
  column: BatchColumn;
  row: BatchRow;
  canEdit: boolean;
  mutations: StoichiometryMutations;
}) {
  // Sync names the product row; everything else names the batch. Keeping the spinner's id on the
  // same anchor as the mutation is what stops two rows of one product sharing a Sync spinner.
  const anchor = column.kind === 'sync' ? row.output.anchor : row.sample.anchor;
  const cell = cellId(anchor, column.id);
  const pending = mutations.savingCells.has(cell);
  const batch = row.sample.shortNbkBatchNumber;
  // Names the cell for assistive tech by what the user sees. The batch number is unique across
  // the notebook, so it identifies the row better than an ordinal does.
  const label = `${column.header}, batch ${batch}`;
  const protectedSample = isSampleProtected(row.sample);

  switch (column.kind) {
    case 'readonly':
      return <ReadonlyCell value={column.value(row)} title={column.title?.(row)} />;
    case 'outputTypeBadge':
      return <OutputTypeBadge value={column.value(row)} />;
    case 'readonlyNumeric':
      return (
        <NumericCell
          value={column.value(row)}
          units={column.units}
          updatedNodes={mutations.updatedNodes}
          editable={false}
          pending={false}
          label={label}
          // Unreachable: `editable` is false, so the input never takes a value to commit.
          onCommit={() => {}}
        />
      );
    case 'numeric':
      return (
        <NumericCell
          value={column.value(row)}
          units={column.units}
          updatedNodes={mutations.updatedNodes}
          // Registration does not freeze these: the backend rejects a registered sample's
          // *compound* mutations only — see `isSampleProtected`.
          editable={canEdit}
          pending={pending}
          label={label}
          onCommit={(next) => mutations.save(cell, column.mutation(row, next))}
        />
      );
    case 'sync':
      return (
        <IconActionCell
          icon={RefreshCw}
          tone="blue"
          // Names the product, because that is what the mutation moves — and the batch, because
          // a product with several batches puts the same button on every one of its rows.
          label={
            row.output.intended
              ? `${row.output.outputName} is already synced with Products (batch ${batch})`
              : `Sync ${row.output.outputName} with Products (batch ${batch})`
          }
          editable={canEdit && !row.output.intended}
          pending={pending}
          onCommit={() => mutations.save(cell, column.mutation(row))}
        />
      );
    case 'register':
      return (
        <IconActionCell
          icon={CircleCheck}
          tone="green"
          label={registerLabel(row, protectedSample, batch)}
          // `RegisterSampleHandler` throws on both of these, so neither is offered.
          editable={canEdit && !protectedSample && row.output.compound.type !== 'UNKNOWN'}
          pending={pending}
          onCommit={() => mutations.save(cell, column.mutation(row))}
        />
      );
    case 'delete':
      return (
        <DeleteCell
          label={`Delete batch ${batch}`}
          // A registered batch is a registry record; indigo-frontend locks it the same way.
          editable={canEdit && !protectedSample}
          pending={pending}
          onCommit={() => mutations.save(cell, column.mutation(row))}
        />
      );
  }
}

/** Why the Register button is disabled, when it is — a disabled icon explains nothing by itself. */
function registerLabel(row: BatchRow, protectedSample: boolean, batch: string): string {
  if (row.sample.registrationStatus === 'REGISTERED') return `Batch ${batch} is already registered`;
  if (protectedSample) return `Batch ${batch} has already been sent for registration`;
  if (row.output.compound.type === 'UNKNOWN') return `Batch ${batch} has no compound to register`;
  return `Register batch ${batch}`;
}
