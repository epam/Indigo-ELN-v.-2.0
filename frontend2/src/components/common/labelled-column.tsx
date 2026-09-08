import type { ReactNode } from 'react';

/**
 * A label above its value, as one cell of a row's column grid. Shared by `ProjectRow` and
 * `DictionaryRow`, which are the same list row with different columns in it.
 *
 * `min-w-0` on the column and `overflow-hidden` on the value are what let the `truncate` inside
 * actually truncate: a grid item's default `min-width: auto` refuses to shrink below its content.
 */
export function LabelledColumn({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex min-w-0 flex-col">
      <dt className="truncate text-[14px]/6 text-neutral-800">{label}</dt>
      <dd className="flex items-center gap-2 overflow-hidden text-[14px]/6">{children}</dd>
    </div>
  );
}
