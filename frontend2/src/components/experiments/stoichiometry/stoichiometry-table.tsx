import { ChevronDown, ChevronRight, Plus, Search, Settings, SquarePlus } from 'lucide-react';
import { useMemo, useState } from 'react';

import {
  DeleteCell,
  DictionaryCell,
  EmptyCell,
  FormulaCell,
  LimitingCell,
  MultiDictionaryCell,
  ReadonlyCell,
  RoleCell,
  TextCell,
} from '@/components/experiments/stoichiometry/cells';
import type { InputColumn, SampleColumn } from '@/components/experiments/stoichiometry/columns';
import {
  COMPOUND_COLUMNS,
  INPUT_ROLES,
  SAMPLE_COLUMNS,
  SAMPLE_INDENT_SPAN,
  shortBatchNumber,
} from '@/components/experiments/stoichiometry/columns';
import { NumericCell } from '@/components/experiments/stoichiometry/numeric-cell';
import type { StoichiometryMutations } from '@/components/experiments/stoichiometry/use-stoichiometry-mutations';
import { cellId, useStoichiometryMutations } from '@/components/experiments/stoichiometry/use-stoichiometry-mutations';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Menu, MenuContent, MenuItem, MenuTrigger } from '@/components/ui/menu';
import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import { canEditExperiment } from '@/lib/types/experiments.ts';
import { cn } from '@/lib/utils';
import type { Reaction, ReactionInput, ReactionInputSample } from '@/lib/types/reactions.ts';

/** The values the backend accepts — `@Min(1) @Max(5)` on `SetExperimentSignificantFigures`. */
const SIGNIFICANT_FIGURES = [1, 2, 3, 4, 5];

const CELL_CLASS = 'border-b border-neutral-300 px-2 py-1 align-middle';
const HEADER_CELL_CLASS =
  'border-y border-neutral-300 px-2 py-2 text-center text-[12px]/5 font-semibold whitespace-nowrap text-neutral-800';

/** Every field the search box looks at. */
function inputHaystack(input: ReactionInput): string {
  const compound = input.compound;
  return [
    input.chemicalName,
    compound.formula,
    compound.type === 'UNKNOWN' ? undefined : compound.compoundKey,
    compound.type === 'UNKNOWN' ? undefined : compound.casNumber,
    ...input.samples.map((sample) => shortBatchNumber(sample.nbkBatchNumber)),
  ]
    .filter((each) => each != null)
    .join(' ')
    .toLowerCase();
}

/**
 * The Reactants, Reagents, Solvents table.
 *
 * The model is a tree — a `ReactionInput` is a compound and owns one or more
 * `ReactionInputSample`s — and this shows it as one: compounds are the top-level rows, and a
 * chevron reveals that compound's batches as nested rows with their own, different columns.
 * indigo-frontend instead rendered one row per (compound × sample) pair with both levels'
 * columns side by side, which repeats every compound field once per batch.
 *
 * **One `<table>`, one grid.** The compound row uses a host column per cell; a sample row spans
 * them, which is what makes the two levels line up without any arithmetic — a cell either starts
 * on a grid boundary or it does not, and the browser cannot render it half a pixel out:
 *
 * ```
 * HOST   | 1 | 2 |   3    |   4    |  5  |    6     |   7   |   8    |   9    | 10  | 11 |   12    |   13    |    14    |    15    | 16 |   17   | 18 | 19  |
 * OUTER  |[v]| # | CompID | Batch# | CAS | ChemName | MolWt | Weight | Volume | Mol | EQ | RxnRole | MolForm | Limiting | SaltCode | ~  | SaltEQ | ~  | del |
 * INNER  |    (indent)    |             Batch #                      | Weight | Volume | Mol | Density | Molarity | Purity |   Hazard Comments   |  Comments | del |
 *                         ^ Batch # aligns                                                                                                             aligns ^
 * ```
 *
 * The two `~` columns are **spacers**: host columns the compound row leaves empty, so the width
 * Hazard Comments and Comments need beyond the columns above them has somewhere to grow that
 * costs nothing. Without them a long comment would inflate Limiting — a radio button — instead.
 *
 * Layout is `auto` and the table is `w-full`, so columns size to their content and the table
 * fills the panel; when the content genuinely needs more room it overflows and the wrapper
 * scrolls. Nothing is capped, so a long value widens its column rather than being truncated.
 *
 * **Every edit is one mutation and there is no Save button**, matching the rest of the
 * experiment screen. The response is a JSON diff rather than a document, so a one-cell edit
 * comes back as a patch touching only what the recalculation moved — and the cells it moved
 * flash, which is the whole reason `updatedNodes` is threaded down to every numeric cell.
 *
 * Nothing here is optimistic: a cell shows what the server last confirmed, and a failed save
 * leaves it alone (`apiFetch` has already raised the toast).
 */
export function StoichiometryTable({ experiment, reaction }: { experiment: ExperimentDetails; reaction: Reaction }) {
  const [search, setSearch] = useState('');
  /**
   * Which rows are **shut**, not which are open. Every row starts expanded — the batches are the
   * point of the table, and a screen of collapsed rows hides the numbers behind a click each —
   * and tracking the negative is what makes that the default for rows that do not exist yet:
   * a row added by `AddEmptyInput` is simply not in this set, so it arrives open like the rest.
   * Seeding an `expanded` set from the current rows would have left every later one shut.
   */
  const [collapsed, setCollapsed] = useState<ReadonlySet<string>>(() => new Set());

  const mutations = useStoichiometryMutations(experiment);
  const canEdit = canEditExperiment(experiment);

  const inputs = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (term === '') return reaction.inputs;
    // Filters compounds, never batches: a compound that matches keeps all of its batches, since
    // hiding some of them would silently change what the numbers on the row add up to.
    return reaction.inputs.filter((input) => inputHaystack(input).includes(term));
  }, [reaction.inputs, search]);

  function toggle(anchor: string) {
    setCollapsed((shut) => {
      const next = new Set(shut);
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

      {/*
        The table is far wider than the panel, so it scrolls horizontally inside its own box
        rather than widening the page. `min-w-max` stops the columns being squeezed into the
        visible width instead of scrolling.
      */}
      <div className="overflow-x-auto rounded-6 border border-neutral-300 bg-card">
        {/*
          `w-full` is a *preferred* width, not a cap: auto layout still expands the table past it
          when the minimum content widths demand more, and the wrapper above scrolls. That is
          "fill the page, scroll when it cannot" from one declaration.
        */}
        <table className="w-full border-collapse">
          <caption className="sr-only">Reactants, reagents and solvents</caption>
          <thead>
            <tr>
              {/* The chevron column has no header text; the chevrons speak for themselves. */}
              <th className={HEADER_CELL_CLASS} />
              {COMPOUND_COLUMNS.map((column) =>
                // A spacer names nothing, so it is a plain cell rather than an empty `<th>`.
                column.kind === 'spacer' ? (
                  <td key={column.id} className={HEADER_CELL_CLASS} />
                ) : (
                  <th key={column.id} scope="col" className={HEADER_CELL_CLASS} style={{ minWidth: column.minWidth }}>
                    {column.header}
                  </th>
                ),
              )}
            </tr>
          </thead>
          {inputs.map((input, index) => (
            <CompoundRow
              key={input.anchor}
              input={input}
              index={index}
              expanded={!collapsed.has(input.anchor)}
              onToggle={() => toggle(input.anchor)}
              canEdit={canEdit}
              mutations={mutations}
            />
          ))}
        </table>

        {inputs.length === 0 && (
          <p className="py-10 text-center text-[14px]/6 text-neutral-700">
            {reaction.inputs.length === 0 ? 'No material added' : 'No material matches this search'}
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
  const significantFigures = experiment.model.significantFigures;

  return (
    <div className="flex flex-wrap items-center justify-between gap-3">
      <div className="relative min-w-0 flex-1 sm:max-w-xs">
        <Search aria-hidden className="absolute top-1/2 left-3 size-4 -translate-y-1/2 text-neutral-700" />
        <Input
          type="search"
          aria-label="Search materials"
          placeholder="Search"
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
          className="pl-9"
        />
      </div>

      <div className="flex items-center gap-2">
        {/*
          Significant figures is entirely a server-side setting: the patch comes back with
          every calculated value already re-rendered to the new precision, so there is no
          client-side rounding anywhere in this component.
        */}
        <span className="text-[14px]/6 whitespace-nowrap text-neutral-800">Significant Figures:</span>
        <Menu>
          <MenuTrigger
            aria-label="Significant figures"
            disabled={!canEdit}
            className={cn(
              'flex items-center gap-1 rounded-2 px-2 py-1 text-[14px]/6 text-neutral-1000 outline-none',
              'hover:bg-neutral-200 focus-visible:ring-3 focus-visible:ring-ring/50',
              'disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:bg-transparent',
            )}
          >
            {significantFigures}
            <ChevronDown aria-hidden className="size-4" />
          </MenuTrigger>
          <MenuContent className="min-w-[80px]">
            {SIGNIFICANT_FIGURES.map((figures) => (
              <MenuItem
                key={figures}
                onClick={() => {
                  if (figures !== significantFigures) {
                    mutations.save(cellId('experiment', 'significantFigures'), {
                      type: 'SetExperimentSignificantFigures',
                      significantFigures: figures,
                    });
                  }
                }}
              >
                {figures}
              </MenuItem>
            ))}
          </MenuContent>
        </Menu>

        <Button
          variant="ghost"
          size="icon"
          aria-label="Add empty row"
          disabled={!canEdit}
          onClick={() =>
            mutations.save(cellId(reaction.anchor, 'addInput'), { type: 'AddEmptyInput', anchor: reaction.anchor })
          }
        >
          <Plus />
        </Button>

        {/*
          TODO(add-registered-sample): the `AddInput` mutation takes a registered sample id,
          which means porting indigo-frontend's sample-search slide-in first.
        */}
        <Button variant="ghost" size="icon" aria-label="Add registered material" disabled>
          <SquarePlus />
        </Button>

        {/* TODO(column-settings): show/hide columns. Nothing server-side carries the choice. */}
        <Button variant="ghost" size="icon" aria-label="Table settings" disabled>
          <Settings />
        </Button>
      </div>
    </div>
  );
}

/**
 * One compound and, when open, its batches — a `<tbody>`, which is what groups rows that belong
 * together and keeps the compound with its own batches when anything reorders.
 */
function CompoundRow({
  input,
  index,
  expanded,
  onToggle,
  canEdit,
  mutations,
}: {
  input: ReactionInput;
  index: number;
  expanded: boolean;
  onToggle: () => void;
  canEdit: boolean;
  mutations: StoichiometryMutations;
}) {
  return (
    <tbody>
      {/* No tint for an expanded row: with every row open by default that would colour the
          whole table, and the hover is what the pointer needs to follow a row across it. */}
      <tr className="hover:bg-neutral-100">
        <td className={CELL_CLASS}>
          <button
            type="button"
            onClick={onToggle}
            aria-expanded={expanded}
            aria-label={`${expanded ? 'Hide' : 'Show'} batches of row ${index + 1}`}
            className="rounded-2 p-1 text-neutral-700 outline-none hover:text-neutral-1000 focus-visible:ring-3 focus-visible:ring-ring/50"
          >
            {expanded ? <ChevronDown className="size-4" /> : <ChevronRight className="size-4" />}
          </button>
        </td>
        {COMPOUND_COLUMNS.map((column) => (
          <td key={column.id} className={CELL_CLASS}>
            <CompoundCell column={column} input={input} index={index} canEdit={canEdit} mutations={mutations} />
          </td>
        ))}
      </tr>

      {expanded && (
        <>
          {/*
            The sample columns' own header, repeated inside every open compound — they are a
            different column set from the one in `<thead>`, and a batch's numbers are unreadable
            without it. Each cell spans the host columns it covers; the leading cell is the
            indent that puts Batch # under Batch #.
          */}
          <tr>
            <td colSpan={SAMPLE_INDENT_SPAN} />
            {SAMPLE_COLUMNS.map((column) => (
              <th
                key={column.id}
                scope="col"
                colSpan={column.span}
                className={cn(HEADER_CELL_CLASS, 'border-t-0')}
                style={{ minWidth: column.minWidth }}
              >
                {column.header}
              </th>
            ))}
          </tr>

          {input.samples.map((sample) => (
            <tr key={sample.anchor} className="hover:bg-neutral-100">
              <td colSpan={SAMPLE_INDENT_SPAN} />
              {SAMPLE_COLUMNS.map((column) => (
                <td key={column.id} colSpan={column.span} className={CELL_CLASS}>
                  <SampleCell column={column} sample={sample} canEdit={canEdit} mutations={mutations} />
                </td>
              ))}
            </tr>
          ))}
        </>
      )}
    </tbody>
  );
}

function CompoundCell({
  column,
  input,
  index,
  canEdit,
  mutations,
}: {
  column: InputColumn;
  input: ReactionInput;
  index: number;
  canEdit: boolean;
  mutations: StoichiometryMutations;
}) {
  const cell = cellId(input.anchor, column.id);
  const pending = mutations.savingCells.has(cell);

  switch (column.kind) {
    case 'spacer':
      return null;
    case 'index':
      // `cursor-default` for the same reason as every other read-only cell — see `EmptyCell`.
      return <span className="cursor-default text-[13px]/5 text-neutral-800">{index + 1}</span>;
    case 'readonly':
      return <ReadonlyCell value={column.value(input)} />;
    case 'html':
      return <FormulaCell value={column.value(input)} />;
    case 'text':
      return (
        <TextCell
          // Keyed on the saved value so a patch that rewrites it reseeds the local draft;
          // without this the cell would keep showing a stale draft after a server change.
          key={column.value(input) ?? ''}
          value={column.value(input)}
          editable={canEdit}
          pending={pending}
          label={`${column.header}, row ${index + 1}`}
          onCommit={(next) => mutations.save(cell, column.mutation(input, next))}
        />
      );
    case 'numeric':
      return (
        <NumericCell
          value={column.value(input)}
          units={column.units}
          updatedNodes={mutations.updatedNodes}
          editable={canEdit && (column.editable?.(input) ?? true)}
          pending={pending}
          label={`${column.header}, row ${index + 1}`}
          onCommit={(next) => mutations.save(cell, column.mutation(input, next))}
        />
      );
    case 'role':
      return (
        <RoleCell
          value={column.value(input)}
          roles={INPUT_ROLES}
          editable={canEdit}
          pending={pending}
          onCommit={(next) => mutations.save(cell, column.mutation(input, next))}
        />
      );
    case 'limiting':
      return (
        <LimitingCell
          checked={column.value(input)}
          editable={canEdit}
          pending={pending}
          label={`Limiting reagent, row ${index + 1}`}
          onCommit={() => mutations.save(cell, column.mutation(input))}
        />
      );
    case 'dictionary':
      return (
        <DictionaryCell
          dictionary={column.dictionary}
          value={column.value(input)}
          editable={canEdit && (column.editable?.(input) ?? true)}
          pending={pending}
          label={`${column.header}, row ${index + 1}`}
          onCommit={(next) => mutations.save(cell, column.mutation(input, next))}
        />
      );
    case 'multiDictionary':
      return (
        <MultiDictionaryCell
          dictionary={column.dictionary}
          value={column.value(input)}
          editable={canEdit}
          pending={pending}
          label={`${column.header}, row ${index + 1}`}
          onCommit={(next) => mutations.save(cell, column.mutation(input, next))}
        />
      );
    case 'delete':
      return (
        <DeleteCell
          label={`${column.label} ${index + 1}`}
          editable={canEdit}
          pending={pending}
          onCommit={() => mutations.save(cell, column.mutation(input))}
        />
      );
  }
}

function SampleCell({
  column,
  sample,
  canEdit,
  mutations,
}: {
  column: SampleColumn;
  sample: ReactionInputSample;
  canEdit: boolean;
  mutations: StoichiometryMutations;
}) {
  const cell = cellId(sample.anchor, column.id);
  const pending = mutations.savingCells.has(cell);
  const batch = shortBatchNumber(sample.nbkBatchNumber) ?? '';
  const label = `${column.header}, batch ${batch}`;

  switch (column.kind) {
    // Declared on the shared `Cell` union but unused by the sample columns; a batch has no
    // ordinal of its own to show, no formula, no role, no limiting flag, and no spacer.
    case 'spacer':
    case 'index':
    case 'html':
    case 'role':
    case 'limiting':
      return <EmptyCell />;
    case 'readonly':
      return <ReadonlyCell value={column.value(sample)} />;
    case 'text':
      return (
        <TextCell
          key={column.value(sample) ?? ''}
          value={column.value(sample)}
          editable={canEdit}
          pending={pending}
          label={label}
          onCommit={(next) => mutations.save(cell, column.mutation(sample, next))}
        />
      );
    case 'numeric':
      return (
        <NumericCell
          value={column.value(sample)}
          units={column.units}
          updatedNodes={mutations.updatedNodes}
          editable={canEdit && (column.editable?.(sample) ?? true)}
          pending={pending}
          label={label}
          onCommit={(next) => mutations.save(cell, column.mutation(sample, next))}
        />
      );
    case 'dictionary':
      return (
        <DictionaryCell
          dictionary={column.dictionary}
          value={column.value(sample)}
          editable={canEdit && (column.editable?.(sample) ?? true)}
          pending={pending}
          label={label}
          onCommit={(next) => mutations.save(cell, column.mutation(sample, next))}
        />
      );
    case 'multiDictionary':
      return (
        <MultiDictionaryCell
          dictionary={column.dictionary}
          value={column.value(sample)}
          editable={canEdit}
          pending={pending}
          label={label}
          onCommit={(next) => mutations.save(cell, column.mutation(sample, next))}
        />
      );
    case 'delete':
      return (
        <DeleteCell
          label={`${column.label} ${batch}`}
          editable={canEdit}
          pending={pending}
          onCommit={() => mutations.save(cell, column.mutation(sample))}
        />
      );
  }
}
