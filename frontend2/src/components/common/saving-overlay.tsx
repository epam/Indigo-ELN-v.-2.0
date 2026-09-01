import { Loader2 } from 'lucide-react';
import type { FocusEvent, ReactNode } from 'react';
import { useEffect, useRef } from 'react';

import { useDelayedFlag } from '@/lib/hooks/use-delayed-flag';
import { cn } from '@/lib/utils';

/**
 * Marks a region as being saved: freezes whatever is inside it and floats a spinner over it.
 *
 * The experiment screen has no Save button — every field writes as it is left — so this is the
 * only signal that anything is happening, and it deliberately sits **on the control being saved**
 * rather than in a corner of the page. A page-level spinner is invisible when you are deep in a
 * table, which is the complaint against the Angular original.
 *
 * Scope it to the control, never to the whole panel: saving one field must leave the field the
 * user just tabbed *into* alive. The same component covers a page-level roll-up too — see the
 * undo/redo pair in `experiment-actions.tsx` — because "this region is busy" is the same idea at
 * both sizes.
 *
 * Interaction is blocked with **`inert`**, not a `disabled` prop, because that is what lets one
 * primitive wrap anything: a combobox or a table cell has nothing to plumb a `disabled` through.
 * A control with its own honest disabled state should still use it — `RichTextEditor` does, since
 * `setEditable(false)` is Tiptap's contract and it greys the toolbar buttons individually.
 *
 * `pending` goes through `useDelayedFlag` here rather than at the call sites, so a save that beats
 * the delay shows nothing and no caller has to remember that.
 */
export function SavingOverlay({
  pending,
  spinner = 'trailing',
  className,
  children,
}: {
  pending: boolean;
  /**
   * Where the spinner sits. `trailing` centres it on the field's right edge, landing where a
   * combobox's chevron was; `top` keeps it in the top-right corner, for a control tall enough
   * that centring would drop it onto the content — a rich-text editor's prose; `center` covers a
   * small control group.
   */
  spinner?: 'trailing' | 'top' | 'center';
  className?: string;
  children: ReactNode;
}) {
  const showing = useDelayedFlag(pending);
  const container = useRef<HTMLDivElement>(null);
  /**
   * The descendant that had focus, kept across the blur `inert` itself causes.
   *
   * Cleared only when focus moves somewhere real. Losing focus to `inert` reports a `null`
   * `relatedTarget` — focus went nowhere — which is exactly the case worth remembering, and it is
   * what tells that apart from the user deliberately moving on.
   */
  const focused = useRef<HTMLElement | null>(null);

  /**
   * Hands focus back when the region unfreezes.
   *
   * `inert` blurs whatever it covers, so a save that outlasts the spinner's delay drops the user
   * out of the control they were in — most visibly in the stoichiometry table, where committing a
   * number on the way to its unit picker freezes the cell that picker lives in.
   *
   * Only when focus is still nowhere. If the user has since clicked or tabbed somewhere else,
   * `document.activeElement` is that element rather than `body`, and pulling them back would be
   * the more annoying of the two behaviours.
   */
  useEffect(() => {
    if (showing) return;

    const target = focused.current;
    focused.current = null;
    if (!target?.isConnected || !container.current?.contains(target)) return;
    if (document.activeElement !== document.body && document.activeElement != null) return;

    target.focus();
  }, [showing]);

  function handleFocus(event: FocusEvent<HTMLDivElement>) {
    focused.current = event.target;
  }

  function handleBlur(event: FocusEvent<HTMLDivElement>) {
    // A null `relatedTarget` is focus going nowhere, which is what `inert` does. Anything else is
    // the user leaving of their own accord, and there is then nothing to restore.
    if (event.relatedTarget != null) focused.current = null;
  }

  return (
    /*
      `data-saving` + `group/saving` publish the busy state to anything rendered inside, so a
      control whose own trailing edge would collide with the spinner can stand aside — the
      comboboxes hide their chevron with `group-data-[saving]/saving:invisible`. Driven by
      `showing`, not `pending`, or a fast save would blink the chevron out with no spinner to
      replace it.

      `center` hugs its content: the spinner is centred on this box, so a full-width wrapper
      would put it beside a small control group rather than over it. `className` still wins,
      since twMerge takes the last word.
    */
    <div
      ref={container}
      onFocus={handleFocus}
      onBlur={handleBlur}
      data-saving={showing ? '' : undefined}
      className={cn('group/saving relative', spinner === 'center' && 'w-fit', className)}
      aria-busy={showing || undefined}
    >
      {/*
        `inert` while saving. React 19 takes it as a boolean prop; Base UI already inerts the page
        behind an open dialog, so the mechanism is not new here.
      */}
      <div inert={showing} className={cn('transition-opacity', showing && 'opacity-60')}>
        {children}
      </div>

      {showing && (
        <>
          {/*
            Outside the inert subtree on purpose: `inert` strips its subtree from the
            accessibility tree, which would take this with it. Absolutely positioned, so the
            spinner appearing never reflows the control underneath.
          */}
          <Loader2
            aria-hidden
            className={cn(
              'pointer-events-none absolute animate-spin text-blue-400',
              spinner === 'trailing' && 'top-1/2 right-3 size-4 -translate-y-1/2',
              spinner === 'top' && 'top-2 right-2 size-4',
              spinner === 'center' && 'inset-0 m-auto size-5',
            )}
          />
          {/* The words a sighted user gets from the spinner. The visible state is icon-only. */}
          <span role="status" className="sr-only">
            Saving…
          </span>
        </>
      )}
    </div>
  );
}
