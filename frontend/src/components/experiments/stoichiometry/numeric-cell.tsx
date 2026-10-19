import type { FocusEvent } from 'react';
import { useRef, useState } from 'react';

import { SavingOverlay } from '@/components/common/saving-overlay';
import { determineCellClasses, USER_ENTERED_CLASSES } from '@/components/experiments/stoichiometry/cell-classes';
import { CONTENT_BOX } from '@/components/experiments/stoichiometry/columns';
import { cn } from '@/lib/utils';
import type { ModelPatch } from '@/lib/types/experiments.ts';
import type { EnteredValue } from '@/lib/types/reactions.ts';

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
 * The number's own box while editing — the same geometry as `DISPLAY_BOX`, so the number does
 * not move as the cell is entered, with the field's border on it.
 */
const INPUT_BOX = cn(CONTENT_BOX, 'rounded-2 py-1 text-[13px]/5 tabular-nums');

/** True when focus has genuinely left this cell, rather than moving within it. */
function isExternal(event: FocusEvent<HTMLElement>): boolean {
  return !event.relatedTarget || !event.currentTarget.contains(event.relatedTarget);
}

/**
 * One numeric cell of the stoichiometry table: formatted text that becomes a number input while
 * it has focus. It shows no unit — every value of a quantity is in the experiment's one unit,
 * which the column header names.
 *
 * **Both forms are in the DOM at all times, and CSS decides which one you see** —
 * `:has(input:focus)` on the wrapper. It names the input rather than asking for focus anywhere,
 * so the cell can hold focus with its value on show once the edit is finished. Three things
 * follow from it:
 *
 * - **Tab order is the DOM's.** The input is always mounted at its natural position, and it is
 *   the cell's only tabbable element, so Tab walks a row one cell at a time.
 * - **Clicking the cell needs no handler.** The editor is transparent but on top, and it is a
 *   `<label>` around the input, so a click anywhere on it lands in the input and focus does the
 *   rest.
 * - **The column cannot resize**, because the display stays in flow as the sizer and the editor
 *   is absolutely positioned over it, contributing no width. Pinned by
 *   `EditingDoesNotResizeTheColumn`.
 *
 * **Enter finishes the edit**, exactly as Escape abandons it: the value is sent and the cell goes
 * back to showing it, with focus on the cell rather than in the input — so Tab still reaches the
 * next control, and a save slow enough to freeze the cell hands focus back to the cell rather
 * than reopening the editor over a value already saved.
 *
 * Commit rules: clearing a set value sends `null`, and nothing is sent when the value did not
 * change — so tabbing across a row is silent.
 */
export function NumericCell({
  value,
  patch,
  editable,
  pending,
  label,
  onCommit,
}: {
  value: EnteredValue<string> | undefined;
  /** The last patch to the experiment — see `useModelPatch`. Drives the flash. */
  patch: ModelPatch;
  editable: boolean;
  pending: boolean;
  /** Names the cell for assistive tech — the visible column header is not associated with it. */
  label: string;
  /** `null` means the user cleared the cell. */
  onCommit: (next: string | null) => void;
}) {
  const [draft, setDraft] = useState(value?.value ?? '');

  /**
   * What the last commit sent, and what the cell shows for as long as that save is in flight.
   *
   * The editor closes on commit, so without this the cell would go back to the value the server
   * last confirmed and sit there for a whole round trip showing the *old* number — which reads as
   * the edit having been thrown away.
   *
   * It is read only while `pending`, so nothing has to clear it: that flag belongs to this same
   * save (`savingCells` is keyed by cell), and the next commit overwrites it. When the save
   * settles the model is back in charge — on success its patch has already landed, and on failure
   * the server's value is exactly what should come back. Boxed, so a sent `null` (a clear) is
   * still told apart from nothing sent.
   */
  const [sent, setSent] = useState<{ value: string | null } | null>(null);
  const inFlight = pending ? sent : null;

  const shownValue = inFlight ? inFlight.value : (value?.value ?? null);
  const text = shownValue ?? '—';
  // A value on its way to the server is written as what it is: one the user entered.
  const classes = inFlight ? USER_ENTERED_CLASSES : determineCellClasses(value, patch.nodes);
  /**
   * A flash is a CSS animation, which plays only when its class is *added*. Undo then redo moves
   * the same cells twice running, and the class would simply stay — so a cell this patch rebuilt
   * remounts its value box, keyed by the patch, and the animation starts over.
   */
  const flashKey = patch.nodes.has(value) ? patch.generation : undefined;
  // Only a colour. An em-dash keeps the column's alignment, so an empty cell has the same edge as
  // the numbers above and below it — a column that half-centres itself has no edge to read at all.
  const emptyClass = shownValue == null ? 'text-neutral-700' : undefined;

  /**
   * Set for exactly one blur, by whichever key is leaving the input — Escape and Enter both have
   * to, and the blur handler commits. Escape would otherwise save the value it has just
   * discarded, because the commit reads `draft` from the render already on screen; Enter would
   * send the same edit twice, since nothing has come back from the server to make the second look
   * unchanged. Cleared by the blur it is meant for, and again on entry, so it can never be left
   * set.
   */
  const leaving = useRef(false);

  /** Where focus rests once an edit is finished — see `leave`. */
  const parking = useRef<HTMLSpanElement>(null);

  /**
   * Closes the editor without letting go of the user's place: focus lands on the parking span,
   * so the display comes back — the editor is shown by `:has(input:focus)`, which that span is
   * not — and Tab goes on to the next control. **After the input in the DOM, which is the whole
   * point**: a container holding focus would send the next Tab back into the input inside it.
   */
  function leave() {
    leaving.current = true;
    parking.current?.focus();
  }

  function reseed() {
    setDraft(value?.value ?? '');
  }

  function commit() {
    const trimmed = draft.trim();
    if (trimmed !== '' && trimmed !== value?.value) {
      send(trimmed);
    } else if (trimmed === '' && value?.value != null) {
      send(null);
    }
  }

  /** Sends an edit, and holds on to it for as long as it is on its way — see `sent`. */
  function send(next: string | null) {
    setSent({ value: next });
    onCommit(next);
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
          key={flashKey}
          aria-hidden
          data-slot="numeric-cell-value"
          className={cn(
            DISPLAY_BOX,
            'block whitespace-nowrap group-has-[input:focus]/cell:invisible',
            emptyClass,
            classes,
            // Marks a number that can be changed. No `decoration-*` colour, so it follows the
            // text's own. Not under an em-dash, which would read as a longer dash.
            editable && shownValue != null && 'underline decoration-dashed decoration-[0.5px] underline-offset-4',
          )}
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
            // A read-only cell has nothing to click into, so the overlay steps aside and leaves the
            // value underneath selectable — it is the only way to copy a number out of the table.
            editable ? 'cursor-text' : 'pointer-events-none',
          )}
        >
          <input
            type="number"
            aria-label={label}
            disabled={!editable}
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            /*
              Every way out of the cell passes through here — Tab, Shift+Tab, a click elsewhere —
              since the input is the only thing in it that can hold focus.
            */
            onBlur={(event) => {
              if (leaving.current) {
                leaving.current = false;
                return;
              }
              commit();
              /*
                A null `relatedTarget` is focus going nowhere — a click on blank space — and the
                edit is then finished with as surely as Enter finishes it. Park focus for the same
                reason Enter does: `SavingOverlay` hands focus back to whatever it froze, and what
                it froze would otherwise be the input, so a save slow enough to show its spinner
                reopened the editor over a value already saved.

                The flag `leave` sets is deliberately not used here: the blur that would consume
                it is this one, already in progress, so a flag left standing would swallow the
                *next* edit's commit.
              */
              if (event.relatedTarget == null) parking.current?.focus();
            }}
            /*
              Chrome steps a focused number input on the wheel and keeps the scroll for itself —
              and the blur would then send a number nobody typed. A read-only input does neither,
              so it is read-only for exactly as long as the browser takes to act on the event.
            */
            onWheel={(event) => {
              const input = event.currentTarget;
              input.readOnly = true;
              setTimeout(() => (input.readOnly = false));
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
              } else if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
                // A stepped number is one nobody typed, and the blur would save it.
                event.preventDefault();
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
            Where focus rests once an edit is finished — see `leave`. No width, and `-1` so it is
            never in the tab order itself.
          */}
          <span ref={parking} tabIndex={-1} className="shrink-0 outline-none" />
        </label>
      </div>
    </SavingOverlay>
  );
}
