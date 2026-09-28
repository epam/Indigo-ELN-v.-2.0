import { Input as InputPrimitive } from '@base-ui/react/input';

import { cn } from '@/lib/utils';

import type { ComponentProps, ReactNode } from 'react';

/**
 * The bordered box every text control lives in.
 *
 * No height, no padding, no alignment: those genuinely differ between the controls that share
 * the box. A trailing button wants `items-center` and `pr-1 pl-3`; a segmented one — the search
 * fields — wants `items-stretch` with the padding on the inner inputs instead; and
 * `MultiCombobox` is `min-h-10` rather than `h-10` because its chips wrap.
 */
const INPUT_BOX = 'w-full rounded-md border border-neutral-300 bg-background transition-colors';

/** The focus ring, in its two flavours: the box is the focusable thing, or something inside it is. */
const INPUT_BOX_FOCUS = 'focus-visible:border-blue-400 focus-visible:ring-3 focus-visible:ring-ring/20';
const INPUT_BOX_FOCUS_WITHIN = 'focus-within:border-blue-400 focus-within:ring-3 focus-within:ring-ring/20';

/**
 * For a shell greyed by a `disabled` *prop*. `Input` and `Select` are disabled elements in their
 * own right and keep the `disabled:` pseudo-variant instead.
 */
const INPUT_DISABLED = 'cursor-not-allowed opacity-50';

/**
 * The in-field icon button, pinned inside the box's right edge.
 *
 * `group-data-[saving]/saving:invisible` is part of the recipe rather than an extra: a spinner at
 * the right edge of a field means **this field is being saved**, and that is where `SavingOverlay`
 * puts one, so anything already sitting there has to stand aside. `invisible` and not `hidden`,
 * so the row keeps its width and the spinner lands exactly where the button was.
 */
const INPUT_ACTION =
  'cursor-pointer rounded-2 p-1 text-neutral-700 outline-none hover:text-neutral-1000 ' +
  'focus-visible:ring-3 focus-visible:ring-ring/50 group-data-[saving]/saving:invisible';

function Input({ className, ...props }: InputPrimitive.Props) {
  return (
    <InputPrimitive
      data-slot="input"
      className={cn(
        INPUT_BOX,
        INPUT_BOX_FOCUS,
        'h-10 px-3 text-[14px]/6 text-neutral-1000 outline-none',
        'placeholder:text-neutral-700',
        'disabled:cursor-not-allowed disabled:opacity-50 data-invalid:border-red-200 data-invalid:ring-red-200/20',
        className,
      )}
      {...props}
    />
  );
}

/**
 * An input with buttons inside its right edge — the shell, not the input.
 *
 * The border and the focus ring live here and the input inside is bare, so the buttons are its
 * flex *siblings* rather than an overlay: the space they take is reserved by layout, text can
 * never run underneath them, and there is no `pr-*` to keep in step with how many are showing.
 * `Combobox` and `Select` draw the same box around their own parts.
 */
function InputGroup({
  startIcon,
  disabled,
  className,
  children,
}: {
  /**
   * A decorative icon on the leading edge — the counterpart of a trailing `InputAction`, and
   * `aria-hidden` because a labelled field does not need it announced.
   */
  startIcon?: ReactNode;
  /** Greys the whole shell. The control inside still needs its own `disabled`. */
  disabled?: boolean;
  className?: string;
  children: ReactNode;
}) {
  return (
    <div
      data-slot="input-group"
      className={cn(
        INPUT_BOX,
        INPUT_BOX_FOCUS_WITHIN,
        // `pr-1` assumes a trailing `InputAction`, whose own `p-1` makes the visual inset 8px.
        // A group with nothing on its right edge wants `pr-3` instead, to match `Input`.
        'flex h-10 items-center gap-1 pr-1 pl-3',
        disabled && INPUT_DISABLED,
        className,
      )}
    >
      {/*
        `pr-1` on top of the row's `gap-1` is the 8px this design puts between a leading icon and
        the text: `pl-3` + a `size-4` icon + 4px + 4px lands the text at 36px.
      */}
      {startIcon && (
        <span aria-hidden className="shrink-0 pr-1 text-neutral-700">
          {startIcon}
        </span>
      )}
      {children}
    </div>
  );
}

/** The bare input to put inside an `InputGroup`: no box of its own, since the shell has one. */
function GroupInput({ className, ...props }: InputPrimitive.Props) {
  return (
    <InputPrimitive
      data-slot="group-input"
      className={cn(
        'min-w-0 flex-1 bg-transparent text-[14px]/6 text-neutral-1000 outline-none placeholder:text-neutral-700',
        className,
      )}
      {...props}
    />
  );
}

/** An icon button inside an `InputGroup`. Icon-only, so the label is required. */
function InputAction({
  label,
  className,
  children,
  ...props
}: Omit<ComponentProps<'button'>, 'aria-label'> & { label: string }) {
  return (
    <button type="button" aria-label={label} className={cn(INPUT_ACTION, className)} {...props}>
      {children}
    </button>
  );
}

export { Input, InputGroup, GroupInput, InputAction };
export { INPUT_BOX, INPUT_BOX_FOCUS, INPUT_BOX_FOCUS_WITHIN, INPUT_DISABLED, INPUT_ACTION };
