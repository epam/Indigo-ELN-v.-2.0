import { Plus, Trash2 } from 'lucide-react';
import { Fragment } from 'react';
import type { ReactNode } from 'react';

import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

/**
 * The frame the two list editors share: column headings once, a row of controls per entry with a
 * trash button at its end, and an Add button under the last.
 *
 * The headings are not `<label>`s — one heading stands over a control in every row — so each
 * control names itself with `rowLabel`, from `composite-forms.ts`.
 */
function CompositeRows({
  headers,
  className,
  rowCount,
  renderRow,
  onRemove,
  addLabel,
  onAdd,
}: {
  headers: string[];
  /** The grid's columns: one per header, and an `auto` one for the trash button. */
  className: string;
  rowCount: number;
  /** The row's controls, one per header, as direct children of a fragment. */
  renderRow: (index: number) => ReactNode;
  onRemove: (index: number) => void;
  addLabel: string;
  onAdd: () => void;
}) {
  return (
    <>
      <div className={cn('grid items-center gap-x-3 gap-y-2', className)}>
        {headers.map((header) => (
          <span key={header} className="text-[12px]/5 font-semibold text-neutral-1000">
            {header}
          </span>
        ))}
        <span />
        {Array.from({ length: rowCount }, (_, index) => (
          <Fragment key={index}>
            {renderRow(index)}
            <Button
              // The dialog is a form, and a button in one submits unless told otherwise.
              type="button"
              variant="secondary"
              aria-label={`Delete row ${index + 1}`}
              className="size-10 text-blue-400"
              onClick={() => onRemove(index)}
            >
              <Trash2 />
            </Button>
          </Fragment>
        ))}
      </div>
      <Button type="button" variant="outline" className="h-10 w-full border-blue-400 text-blue-400" onClick={onAdd}>
        <Plus />
        {addLabel}
      </Button>
    </>
  );
}

export { CompositeRows };
