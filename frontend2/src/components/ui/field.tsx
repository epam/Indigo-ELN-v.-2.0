import { cn } from '@/lib/utils';

/**
 * Label above, control, error below — the layout every field in the app shares.
 *
 * Deliberately plain markup rather than Base UI's `Field`: that only wires labels to its
 * own `Field.Control`, which the combobox and the rich-text editor are not. The label
 * carries both `htmlFor` and an id, so a native control is associated the usual way and a
 * `contenteditable` can point back at it with `aria-labelledby`.
 */
function Field({
  id,
  label,
  required,
  error,
  className,
  children,
}: {
  /** Id of the control being labelled; `${id}-label` is the label's own id. */
  id: string;
  label: string;
  required?: boolean;
  error?: string;
  className?: string;
  children: React.ReactNode;
}) {
  return (
    <div className={cn('flex flex-col gap-1.5', className)}>
      <label id={`${id}-label`} htmlFor={id} className="text-[14px]/6 text-neutral-800">
        {label}
        {required && <span className="text-red-200"> *</span>}
      </label>
      {children}
      {error && (
        <p role="alert" className="text-[12px]/5 text-red-200">
          {error}
        </p>
      )}
    </div>
  );
}

export { Field };
