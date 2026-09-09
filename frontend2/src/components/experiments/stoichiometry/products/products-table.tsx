import { useMemo, useState } from 'react';

import {
  DictionaryCell,
  FormulaCell,
  ReadonlyCell,
  RowActions,
  TextCell,
} from '@/components/experiments/stoichiometry/cells';
import {
  ACTIONS_CELL_CLASS,
  ALIGN_CLASS,
  CELL_CLASS,
  CONTENT_BOX,
  HEADER_CELL_CLASS,
  alignOf,
} from '@/components/experiments/stoichiometry/columns';
import { NumericCell } from '@/components/experiments/stoichiometry/numeric-cell';
import { AddBatchCell, OutputTypeCell } from '@/components/experiments/stoichiometry/products/cells';
import type { ProductColumn, ProductRow } from '@/components/experiments/stoichiometry/products/columns';
import { PRODUCT_COLUMNS, productHaystack } from '@/components/experiments/stoichiometry/products/columns';
import type { StoichiometryMutations } from '@/lib/hooks/experiments/use-stoichiometry-mutations';
import { cellId, useStoichiometryMutations } from '@/lib/hooks/experiments/use-stoichiometry-mutations';
import { cn } from '@/lib/utils';
import { SearchInput } from '@/components/ui/search-input';
import { Switch } from '@/components/ui/switch';
import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import { canEditExperiment } from '@/lib/types/experiments.ts';
import { OUTPUT_TYPES } from '@/lib/types/reactions.ts';

/**
 * The Reaction Products table.
 *
 * One row per `ReactionOutput` — flat, unlike the stoichiometry table above it, because an
 * output's samples are the *Product Batch Summary*'s subject rather than this table's. So there
 * is nothing to expand, no column spans, and no spacer columns.
 *
 * **Only `intended` products are listed**, which is what indigo-frontend does too. The flag says
 * the product was drawn in the reaction scheme; an unintended output is something the reaction
 * threw off that a chemist recorded afterwards, and it belongs to the batch summary. It is not
 * the same thing as the Products Type column, which is `FINAL` / `BY_PRODUCT` / `INTERMEDIATE`
 * and is editable on every row here.
 *
 * Products are **created and deleted by editing the reaction scheme**, not from this table:
 * `SetScheme` diffs the drawing and calls `createOutputLine(…, intended = true, …)` for each
 * product molecule, and there is no `RemoveOutputRow` mutation at all. Hence a row's only action
 * is Add Batch, and the toolbar has nothing that adds a row.
 *
 * Every edit is one mutation and there is no Save button, matching the rest of the screen.
 * Nothing is optimistic: a cell shows what the server last confirmed, and a failed save leaves
 * the draft alone (`apiFetch` has already raised the toast).
 */
export function ReactionProductsTable({ experiment, step }: { experiment: ExperimentDetails; step: number }) {
  const [search, setSearch] = useState('');
  const [showAllSteps, setShowAllSteps] = useState(false);

  const mutations = useStoichiometryMutations(experiment);
  const canEdit = canEditExperiment(experiment);
  const reactions = experiment.model.reactions;

  const products = useMemo(() => {
    const steps = showAllSteps
      ? reactions.map((reaction, index) => ({ reaction, index }))
      : [{ reaction: reactions[step], index: step }];
    return steps.flatMap(({ reaction, index }) =>
      reaction.outputs.filter((output) => output.intended).map((output) => ({ output, step: index })),
    );
  }, [reactions, step, showAllSteps]);

  const rows = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (term === '') return products;
    return products.filter((row) => productHaystack(row).includes(term));
  }, [products, search]);

  return (
    <div className="flex flex-col gap-3">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <SearchInput
          aria-label="Search products"
          value={search}
          onChange={setSearch}
          className="min-w-0 flex-1 sm:max-w-xs"
        />

        <div className="flex items-center gap-3">
          {/*
            Purely a client-side filter over `model.reactions` — no request, and nothing
            server-side records the choice. It reads as a no-op today because an experiment has
            exactly one step: there is no `AddReaction` mutation, which is also why the step
            strip in `StoichiometryPanel` is gated off. The Reaction Step column is what makes
            it legible once there is more than one.
          */}
          <label className="flex items-center gap-2 text-[14px]/6 whitespace-nowrap text-neutral-800">
            <Switch checked={showAllSteps} onCheckedChange={setShowAllSteps} />
            Show All Steps
          </label>
        </div>
      </div>

      <div className="overflow-x-auto rounded-6 border border-neutral-300 bg-card">
        {/*
          `w-full` is a preferred width, not a cap — see the note in `StoichiometryTable`, and
          `border-separate` for why the pinned actions column needs it.
        */}
        <table className="w-full border-separate border-spacing-0">
          <caption className="sr-only">Reaction products</caption>
          <thead>
            <tr>
              {PRODUCT_COLUMNS.map((column) => (
                <th
                  key={column.id}
                  scope="col"
                  className={cn(
                    HEADER_CELL_CLASS,
                    ALIGN_CLASS[alignOf(column.kind)],
                    column.kind === 'addBatch' && ACTIONS_CELL_CLASS,
                  )}
                  style={{ minWidth: column.minWidth }}
                >
                  {column.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {/* `group/row` — the pinned actions cell paints its own background, so it cannot
                inherit the row's hover and follows it explicitly. */}
            {rows.map((row, index) => (
              <tr key={row.output.anchor} className="group/row hover:bg-neutral-100">
                {PRODUCT_COLUMNS.map((column) => (
                  <td
                    key={column.id}
                    className={cn(
                      CELL_CLASS,
                      ALIGN_CLASS[alignOf(column.kind)],
                      column.kind === 'addBatch' && ACTIONS_CELL_CLASS,
                    )}
                  >
                    <ProductCell column={column} row={row} index={index} canEdit={canEdit} mutations={mutations} />
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>

        {rows.length === 0 && (
          <p className="py-10 text-center text-[14px]/6 text-neutral-700">
            {products.length === 0 ? 'No product added' : 'No product matches this search'}
          </p>
        )}
      </div>
    </div>
  );
}

function ProductCell({
  column,
  row,
  index,
  canEdit,
  mutations,
}: {
  column: ProductColumn;
  row: ProductRow;
  index: number;
  canEdit: boolean;
  mutations: StoichiometryMutations;
}) {
  const cell = cellId(row.output.anchor, column.id);
  const pending = mutations.savingCells.has(cell);
  // Names the cell for assistive tech by what the user sees, not by the row number: with Show
  // All Steps on, two steps' rows are interleaved and an ordinal says nothing about which.
  const label = `${column.header}, ${row.output.outputName}`;

  switch (column.kind) {
    case 'index':
      // `cursor-default` for the same reason as every other read-only cell — see `EmptyCell`.
      return (
        <span className={cn(CONTENT_BOX, 'block cursor-default text-[13px]/5 text-neutral-800')}>{index + 1}</span>
      );
    case 'readonly':
      return <ReadonlyCell value={column.value(row)} />;
    case 'html':
      return <FormulaCell value={column.value(row)} />;
    case 'text':
      return (
        <TextCell
          // Keyed on the saved value so a patch that rewrites it reseeds the local draft.
          key={column.value(row) ?? ''}
          value={column.value(row)}
          editable={canEdit}
          pending={pending}
          label={label}
          onCommit={(next) => mutations.save(cell, column.mutation(row, next))}
        />
      );
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
          editable={canEdit && (column.editable?.(row) ?? true)}
          pending={pending}
          label={label}
          onCommit={(next) => mutations.save(cell, column.mutation(row, next))}
        />
      );
    case 'dictionary':
      return (
        <DictionaryCell
          dictionary={column.dictionary}
          value={column.value(row)}
          editable={canEdit && (column.editable?.(row) ?? true)}
          pending={pending}
          label={label}
          onCommit={(next) => mutations.save(cell, column.mutation(row, next))}
        />
      );
    case 'outputType':
      return (
        <OutputTypeCell
          value={column.value(row)}
          types={OUTPUT_TYPES}
          editable={canEdit}
          pending={pending}
          label={label}
          onCommit={(next) => mutations.save(cell, column.mutation(row, next))}
        />
      );
    case 'addBatch':
      // A single action still goes through `RowActions` — the packing belongs to the column, not
      // to how many buttons happen to be in it.
      return (
        <RowActions>
          <AddBatchCell
            label={`Add batch to ${row.output.outputName}`}
            editable={canEdit}
            pending={pending}
            onCommit={() => mutations.save(cell, column.mutation(row))}
          />
        </RowActions>
      );
  }
}
