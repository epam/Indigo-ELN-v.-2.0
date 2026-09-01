import type { FocusEvent } from 'react';
import { useRef, useState } from 'react';

import { SavingOverlay } from '@/components/common/saving-overlay';
import { determineCellClasses } from '@/components/experiments/stoichiometry/cell-classes';
import { unitLabel } from '@/components/experiments/stoichiometry/units';
import { cn } from '@/lib/utils';
import type { EnteredValue } from '@/lib/types/reactions.ts';

/** What a committed edit carries. `null` for both means the user cleared the cell. */
export interface NumericCellValue {
  value: string | null;
  unit: string | null;
}

/**
 * The typography and box the display text and the number input share. They sit on top of one
 * another and swap by opacity, so any disagreement here shows up as the text shifting under the
 * cursor — neither gets to spell it out for itself.
 */
const DISPLAY_BOX = 'w-full rounded-2 border border-transparent px-2 py-1 text-right text-[13px]/5 tabular-nums';

/** True when focus has genuinely left this cell, rather than moving between its own controls. */
function isExternal(event: FocusEvent<HTMLElement>): boolean {
  return !event.relatedTarget || !event.currentTarget.contains(event.relatedTarget);
}

/**
 * One numeric cell of the stoichiometry table: formatted text that becomes a number input and a
 * unit picker while it has focus.
 *
 * **Both forms are in the DOM at all times, and CSS decides which one you see** —
 * `group-focus-within` on the wrapper. That is the whole design, and four things follow from it
 * that used to be done by hand:
 *
 * - **Nothing calls `focus()`.** The input is always mounted at its natural position, so tab
 *   order across a row is the DOM's and needs no help.
 * - **Clicking the cell needs no handler.** The editor is transparent but on top, so a click
 *   lands on the input and focus does the rest.
 * - **There is no `editing` flag** for a stale ref to disagree with. The previous version tracked
 *   the unit menu's open state and a guard against reopening it, both of which outlived the
 *   elements they described — a committed edit unmounted the menu while the flags stayed set, so
 *   the picker then refused to open and blur stopped committing.
 * - **The column cannot resize**, because the display stays in flow as the sizer and the editor
 *   is absolutely positioned over it, contributing no width. Pinned by
 *   `EditingDoesNotResizeTheColumn`.
 *
 * The unit picker is a **native `<select>`** rather than a popup: its list is drawn by the OS
 * instead of being mounted in the document, so focus never leaves the cell and the blur handler
 * can trust what it is told. The cost is that a native list cannot be opened from script — so
 * where this used to force the picker open when a number was typed without a unit, Tab now simply
 * lands on it, which asks the same question earlier and without any code.
 *
 * Commit rules, from indigo-frontend's `editable-data-table.component.ts`: value and unit go
 * together or not at all, clearing a set value sends `null` for both, and nothing is sent when
 * neither half changed — so tabbing across a row is silent.
 */
export function NumericCell({
  value,
  units,
  updatedNodes,
  editable,
  pending,
  label,
  onCommit,
}: {
  value: EnteredValue<string> | undefined;
  /** The units this quantity can take. A single-element list renders no picker. */
  units: readonly string[];
  updatedNodes: ReadonlyMap<unknown, unknown>;
  editable: boolean;
  pending: boolean;
  /** Names the cell for assistive tech — the visible column header is not associated with it. */
  label: string;
  onCommit: (next: NumericCellValue) => void;
}) {
  const unitless = units.length === 1;

  // A unitless quantity has exactly one legal unit, so seeding it is not a guess.
  const storedUnit = () => value?.unit ?? (unitless ? units[0] : null);
  const [draft, setDraft] = useState(value?.value ?? '');
  const [draftUnit, setDraftUnit] = useState<string | null>(storedUnit);

  const classes = determineCellClasses(value, updatedNodes);
  const text = value?.value == null ? '—' : `${value.value}${unitless ? '' : ` ${unitLabel(value.unit)}`}`;
  const emptyClass = value?.value == null ? 'text-center text-neutral-700' : undefined;

  /**
   * Set for exactly one blur, by Escape. Reverting has to blur to get back to the display, but
   * the blur handler commits — and it would read `draft` from the render that is already on
   * screen, i.e. the value Escape just discarded, so Escape would save it. Cleared by the blur it
   * is meant for, and again on entry, so it can never be left set the way the flags this
   * component used to carry were.
   */
  const reverting = useRef(false);

  function reseed() {
    setDraft(value?.value ?? '');
    setDraftUnit(storedUnit());
  }

  function commit(unit = draftUnit) {
    const trimmed = draft.trim();
    const nextSet = trimmed !== '' && unit != null;
    const prevSet = value?.value != null && value.unit != null;

    if (nextSet && (trimmed !== value?.value || unit !== value?.unit)) {
      onCommit({ value: trimmed, unit });
    } else if (!nextSet && prevSet) {
      onCommit({ value: null, unit: null });
    }
  }

  return (
    <SavingOverlay pending={pending} spinner="center" className="w-full">
      {/*
        Entering and leaving are symmetric and both gated on `isExternal`, because the cell has
        two controls and moving between them is neither. Seeding on entry rather than on every
        focus is what stops tabbing back from the unit picker wiping what was just typed.
      */}
      <div
        data-slot="numeric-cell"
        className="group/cell relative"
        // Entry only. Leaving is the input's own business — see its `onBlur`.
        onFocus={(event) => {
          if (!isExternal(event)) return;
          reverting.current = false;
          reseed();
        }}
      >
        {/*
          In flow, so this is what the column measures — and `invisible` rather than `hidden`
          while editing, which keeps the box and its width while taking the text out of the paint.
          `aria-hidden` because the input below carries the same value and the real label; without
          it the cell would be announced twice.
        */}
        <span
          aria-hidden
          data-slot="numeric-cell-value"
          className={cn(DISPLAY_BOX, 'block group-focus-within/cell:invisible', emptyClass, classes)}
        >
          {text}
        </span>

        {/*
          Out of flow, so nothing here contributes to the column's width. Transparent until the
          cell has focus, but still hit-testable — which is what makes a click land on the input
          with no click handler anywhere.
        */}
        <div className="absolute inset-0 flex items-center gap-1 opacity-0 group-focus-within/cell:opacity-100">
          <input
            type="number"
            aria-label={label}
            disabled={!editable}
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            /*
              Commits whenever the **input** loses focus, not when the cell does — including on
              the way to the unit picker beside it. A value that already had a unit is a finished
              edit the moment the number changes, and nothing else on this screen confirms one:
              holding it until focus left the whole cell meant tabbing to the unit looked like it
              had done nothing.

              Every way out of the input passes through here, so the wrapper needs no blur
              handler of its own. Leaving mid-edit is still safe: `commit` sends nothing unless
              both halves are set, so a number typed into a cell with no unit yet waits for one.
            */
            onBlur={() => {
              if (reverting.current) {
                reverting.current = false;
                return;
              }
              commit();
            }}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                event.preventDefault();
                commit();
              } else if (event.key === 'Escape') {
                event.preventDefault();
                // Blur as well as revert: leaving focus in the input would keep the editor up
                // over a value the user has just abandoned.
                reverting.current = true;
                reseed();
                event.currentTarget.blur();
              }
            }}
            className={cn(
              DISPLAY_BOX,
              'min-w-0 border-blue-400 bg-background outline-none',
              // Editability decides the cursor, not whether the cell has a value.
              editable ? 'cursor-text' : 'cursor-default',
              // The spinners would eat most of the width of a cell this narrow.
              '[appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none',
            )}
          />
          {!unitless && (
            <select
              aria-label={`${label} unit`}
              disabled={!editable}
              value={draftUnit ?? ''}
              onChange={(event) => {
                const next = event.target.value || null;
                setDraftUnit(next);
                // Picking is the commit gesture, as it is for every other picker on this screen.
                // Passed in rather than read back: the state set above is not visible yet.
                commit(next);
              }}
              className="shrink-0 cursor-pointer rounded-2 bg-background py-1 pl-1 text-[13px]/5 text-neutral-800 outline-none focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-default"
            >
              {/* Only offered until a unit is chosen: the backend has no "no unit" for these. */}
              {draftUnit == null && <option value="">—</option>}
              {units.map((unit) => (
                <option key={unit} value={unit}>
                  {unitLabel(unit)}
                </option>
              ))}
            </select>
          )}
        </div>
      </div>
    </SavingOverlay>
  );
}
