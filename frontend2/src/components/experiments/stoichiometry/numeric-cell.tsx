import type { FocusEvent, RefObject } from 'react';
import { useLayoutEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';

import { SavingOverlay } from '@/components/common/saving-overlay';
import { determineCellClasses } from '@/components/experiments/stoichiometry/cell-classes';
import { CONTENT_BOX } from '@/components/experiments/stoichiometry/columns';
import { unitLabel } from '@/lib/types/reactions.ts';
import { cn } from '@/lib/utils';
import type { EnteredValue } from '@/lib/types/reactions.ts';

/** What a committed edit carries. `null` for both means the user cleared the cell. */
export interface NumericCellValue {
  value: string | null;
  unit: string | null;
}

/**
 * The typography and box the display text and the editor share. They sit on top of one another
 * and swap by opacity, so any disagreement here shows up as the text shifting under the cursor —
 * neither gets to spell it out for itself.
 *
 * No `text-align`: the cell inherits the column's, which the `<td>` carries (`alignOf`). Spelling
 * one out here would be a second opinion on where a number sits, and the header would be the one
 * that disagreed.
 */
const DISPLAY_BOX = cn(CONTENT_BOX, 'w-full rounded-2 py-1 text-[13px]/5 tabular-nums');

/**
 * The number's own box while editing. The same geometry as `DISPLAY_BOX` — so the number does not
 * move as the cell is entered — but the border is on **the input**: what is being typed into is
 * what should look like a field, and a border drawn round the unit as well made the two read as
 * one box with a list inexplicably inside it.
 */
const INPUT_BOX = cn(CONTENT_BOX, 'rounded-2 py-1 text-[13px]/5 tabular-nums');

/** How close to the window's edge the unit list may come. */
const EDGE = 8;

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}

/**
 * The unit's share of a numeric cell — a fifth, but never less than the widest unit label, which
 * is what the narrow columns fall back to. The number takes what is left.
 */
const UNIT_SLOT = 'w-1/5 min-w-12 shrink-0 text-[13px]/5';

/** True when focus has genuinely left this cell, rather than moving within it. */
function isExternal(event: FocusEvent<HTMLElement>): boolean {
  return !event.relatedTarget || !event.currentTarget.contains(event.relatedTarget);
}

/**
 * One numeric cell of the stoichiometry table: formatted text that becomes a number input while
 * it has focus, with the unit list dropped down beneath it.
 *
 * **Both forms are in the DOM at all times, and CSS decides which one you see** —
 * `:has(input:focus)` on the wrapper. It names the input rather than asking for focus anywhere,
 * so the cell can hold focus with its value on show once the edit is finished. Three things
 * follow from it:
 *
 * - **Tab order is the DOM's.** The input is always mounted at its natural position, and it is
 *   the cell's only focusable element, so Tab walks a row one cell at a time.
 * - **Clicking the cell needs no handler.** The editor is transparent but on top, and it is a
 *   `<label>` around the input, so a click anywhere on it — the unit text included — lands in the
 *   input and focus does the rest.
 * - **The column cannot resize**, because the display stays in flow as the sizer and the editor
 *   is absolutely positioned over it, contributing no width. Pinned by
 *   `EditingDoesNotResizeTheColumn`.
 *
 * **The unit is chosen without leaving the number.** Where there is more than one, a list opens
 * under the cell's right edge — where the unit is read — for as long as the input has focus, and
 * it is the only place the chosen unit is shown: ArrowUp/ArrowDown step through it, and a
 * click picks an option without taking focus (its `mousedown` is cancelled). Nothing in it is
 * tabbable. It is portalled, because it hangs outside the cell and the table's `overflow` would
 * clip it — which is also why it follows a `focused` flag rather than `:focus-within`, which
 * cannot reach into a portal.
 *
 * A single-unit quantity has nothing to choose, so it shows its unit as plain text: the column's
 * `suffix` where it has one (`%` for purity, nothing for molecular weight), else the unit's label.
 *
 * **Enter finishes the edit**, exactly as Escape abandons it: the value is sent and the cell goes
 * back to showing it, with focus on the cell rather than in the input — so Tab still reaches the
 * next control, and a save slow enough to freeze the cell hands focus back to the cell rather
 * than reopening the editor over a value already saved.
 *
 * Commit rules, from indigo-frontend's `editable-data-table.component.ts`: value and unit go
 * together or not at all, clearing a set value sends `null` for both, and nothing is sent when
 * neither half changed — so tabbing across a row is silent. Choosing a unit only changes the
 * draft; the edit is sent when the input is left or Enter is pressed.
 */
export function NumericCell({
  value,
  units,
  suffix,
  updatedNodes,
  editable,
  pending,
  label,
  onCommit,
}: {
  value: EnteredValue<string> | undefined;
  /** The units this quantity can take. A single-element list renders no picker. */
  units: readonly string[];
  /** The text of a single-unit quantity's unit. Defaults to that unit's label. */
  suffix?: string;
  updatedNodes: ReadonlyMap<unknown, unknown>;
  editable: boolean;
  pending: boolean;
  /** Names the cell for assistive tech — the visible column header is not associated with it. */
  label: string;
  onCommit: (next: NumericCellValue) => void;
}) {
  const unitless = units.length === 1;
  const fixedUnit = suffix ?? unitLabel(units[0]);

  // A unitless quantity has exactly one legal unit, so seeding it is not a guess.
  const storedUnit = () => value?.unit ?? (unitless ? units[0] : null);
  const [draft, setDraft] = useState(value?.value ?? '');
  const [draftUnit, setDraftUnit] = useState<string | null>(storedUnit);
  const [focused, setFocused] = useState(false);

  const classes = determineCellClasses(value, updatedNodes);
  const shownUnit = unitless ? fixedUnit : unitLabel(value?.unit);
  const text = value?.value == null ? '—' : `${value.value}${shownUnit && ` ${shownUnit}`}`;
  // Only a colour. An em-dash keeps the column's alignment, so an empty cell has the same edge as
  // the numbers above and below it — a column that half-centres itself has no edge to read at all.
  const emptyClass = value?.value == null ? 'text-neutral-700' : undefined;

  /**
   * Set for exactly one blur, by whichever key is leaving the input — Escape and Enter both have
   * to, and the blur handler commits. Escape would otherwise save the value it has just
   * discarded, because the commit reads `draft` from the render already on screen; Enter would
   * send the same edit twice, since nothing has come back from the server to make the second look
   * unchanged. Cleared by the blur it is meant for, and again on entry, so it can never be left
   * set.
   */
  const leaving = useRef(false);

  /** The unit's box, which is what the list opens over. */
  const unitSlot = useRef<HTMLSpanElement>(null);

  /**
   * Closes the editor without letting go of the user's place: focus lands on the unit box, so the
   * display comes back — the editor is shown by `:has(input:focus)`, which that box is not — and
   * Tab goes on to the next control. **After the input in the DOM, which is the whole point**: a
   * container holding focus would send the next Tab back into the input inside it.
   */
  function leave() {
    leaving.current = true;
    unitSlot.current?.focus();
  }

  function reseed() {
    setDraft(value?.value ?? '');
    setDraftUnit(storedUnit());
  }

  function commit() {
    const trimmed = draft.trim();
    const nextSet = trimmed !== '' && draftUnit != null;
    const prevSet = value?.value != null && value.unit != null;

    if (nextSet && (trimmed !== value?.value || draftUnit !== value?.unit)) {
      onCommit({ value: trimmed, unit: draftUnit });
    } else if (!nextSet && prevSet) {
      onCommit({ value: null, unit: null });
    }
  }

  /** Steps the draft unit along `units`, stopping at either end. */
  function stepUnit(delta: 1 | -1) {
    const index = draftUnit == null ? -1 : units.indexOf(draftUnit);
    const next = index === -1 ? (delta === 1 ? 0 : units.length - 1) : index + delta;
    if (next >= 0 && next < units.length) setDraftUnit(units[next]);
  }

  return (
    <SavingOverlay pending={pending} spinner="center" className="w-full">
      <div
        data-slot="numeric-cell"
        className="group/cell relative"
        // Entry only. Leaving is the input's own business — see its `onBlur`.
        onFocus={(event) => {
          if (!isExternal(event)) return;
          leaving.current = false;
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
          className={cn(DISPLAY_BOX, 'block group-has-[input:focus]/cell:invisible', emptyClass, classes)}
        >
          {text}
        </span>

        {/*
          Out of flow, so nothing here contributes to the column's width. Transparent until the
          cell has focus, but still hit-testable — which, with the `<label>`, is what makes a click
          land on the input with no click handler anywhere.
        */}
        <label
          className={cn(
            'absolute inset-0 flex items-stretch opacity-0 group-has-[input:focus]/cell:opacity-100',
            // Editability decides the cursor, not whether the cell has a value.
            editable ? 'cursor-text' : 'cursor-default',
          )}
        >
          <input
            type="number"
            aria-label={label}
            disabled={!editable}
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            onFocus={() => setFocused(true)}
            /*
              Every way out of the cell passes through here — Tab, Shift+Tab, a click elsewhere —
              since the input is the only thing in it that can hold focus. Leaving mid-edit is
              safe: `commit` sends nothing unless both halves are set.
            */
            onBlur={(event) => {
              setFocused(false);
              if (leaving.current) {
                leaving.current = false;
                return;
              }
              commit();
              /*
                A null `relatedTarget` is focus going nowhere — a click on blank space — and the
                edit is then finished with as surely as Enter finishes it. Park focus on the unit
                box for the same reason Enter does: `SavingOverlay` hands focus back to whatever
                it froze, and what it froze would otherwise be the input, so a save slow enough to
                show its spinner reopened the editor over a value already saved.

                The flag `leave` sets is deliberately not used here: the blur that would consume
                it is this one, already in progress, so a flag left standing would swallow the
                *next* edit's commit.
              */
              if (event.relatedTarget == null) unitSlot.current?.focus();
            }}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                event.preventDefault();
                commit();
                // A committed value is a finished edit, so the cell goes back to showing it.
                leave();
              } else if (event.key === 'Escape') {
                event.preventDefault();
                // Leaving as well as reverting: focus left in the input would keep the editor up
                // over a value the user has just abandoned.
                reseed();
                leave();
              } else if (!unitless && (event.key === 'ArrowDown' || event.key === 'ArrowUp')) {
                // Also what stops the browser stepping the number itself.
                event.preventDefault();
                stepUnit(event.key === 'ArrowDown' ? 1 : -1);
              }
            }}
            className={cn(
              INPUT_BOX,
              // `text-align: inherit` rather than a `text-right` of its own: an input does not
              // inherit it on its own, so the number sat left of where the display text was.
              'min-w-0 flex-1 border-blue-400 bg-background outline-none [text-align:inherit]',
              // The spinners would eat most of the width of a cell this narrow.
              '[appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none',
            )}
          />
          {/*
            The unit's place, kept whether or not anything is in it: a fixed unit is written here,
            and where there is a list the list opens from here. A fifth of the cell, floored at the
            widest label there is, so the number keeps the rest. It sits **outside** the input's
            border and flush against it, which is where the list then attaches.
          */}
          <span
            ref={unitSlot}
            // `-1`, so it is never in the tab order itself: it is only ever focused by `leave`.
            tabIndex={-1}
            className={cn(UNIT_SLOT, 'flex items-center justify-end pr-2 pl-1 text-neutral-800 outline-none')}
          >
            {unitless && fixedUnit}
          </span>
        </label>

        {!unitless && focused && editable && (
          <UnitList
            anchor={unitSlot}
            units={units}
            selected={draftUnit}
            label={`${label} unit`}
            onSelect={setDraftUnit}
          />
        )}
      </div>
    </SavingOverlay>
  );
}

/**
 * The unit options, over the cell's unit box for as long as the number has focus.
 *
 * Portalled with `position: fixed` rather than a Base UI `Popover`: the popover's focus guards are
 * tabbable, and Tab out of the number landed on one of them instead of the next cell. Nothing
 * here can take focus — each option cancels its `mousedown` — so the input keeps it throughout.
 *
 * **It takes the unit box's place and width**, so the options land exactly where the unit is read
 * and the list stays inside the column rather than over the one next door, attached to the right
 * of the input's border with nothing between them. Its first option is on the number's own line
 * whichever one is chosen — the chosen one is marked by its highlight rather than by its place —
 * and the rest hang below.
 *
 * The position is re-read on any scroll (captured, so the table's own horizontal scroll counts
 * too) and on resize, which is all it takes for the list to follow a cell that moves, and it is
 * held inside the window at either end. Measuring itself is what makes both possible, so it
 * renders hidden for one pass and the effect below places it; `update` returns the previous state
 * when nothing moved, or an effect with no dep array would loop.
 */
function UnitList({
  anchor,
  units,
  selected,
  label,
  onSelect,
}: {
  anchor: RefObject<HTMLElement | null>;
  units: readonly string[];
  selected: string | null;
  label: string;
  onSelect: (unit: string) => void;
}) {
  const list = useRef<HTMLDivElement>(null);
  const [position, setPosition] = useState<{ top: number; left: number; width: number } | null>(null);

  useLayoutEffect(() => {
    function update() {
      const rect = anchor.current?.getBoundingClientRect();
      if (!rect) return;
      const height = list.current?.offsetHeight ?? 0;
      // Hangs upwards instead where the window's bottom edge leaves no room for it.
      const top = clamp(rect.top, EDGE, window.innerHeight - height - EDGE);
      const next = { top, left: rect.left, width: rect.width };
      setPosition((prev) => (prev?.top === top && prev.left === next.left && prev.width === next.width ? prev : next));
    }
    update();
    window.addEventListener('scroll', update, true);
    window.addEventListener('resize', update);
    return () => {
      window.removeEventListener('scroll', update, true);
      window.removeEventListener('resize', update);
    };
  }, [anchor]);

  return createPortal(
    <div
      ref={list}
      role="listbox"
      aria-label={label}
      // Hidden rather than absent for the first pass: it has to be laid out to be measured.
      style={position ?? { top: 0, left: 0, visibility: 'hidden' }}
      className="fixed z-50 overflow-hidden rounded-2 border border-neutral-300 bg-popover text-right shadow-card"
    >
      {units.map((unit) => (
        <div
          key={unit}
          role="option"
          aria-selected={unit === selected}
          // Keeps focus in the input, so picking a unit is not leaving the cell.
          onMouseDown={(event) => event.preventDefault()}
          onClick={() => onSelect(unit)}
          className={cn(
            // `py-1` and the 20px line make an option exactly as tall as the input beside it.
            'cursor-default px-2 py-1 text-[13px]/5 hover:bg-neutral-100',
            unit === selected && 'bg-blue-10 hover:bg-blue-10',
          )}
        >
          {unitLabel(unit)}
        </div>
      ))}
    </div>,
    document.body,
  );
}
