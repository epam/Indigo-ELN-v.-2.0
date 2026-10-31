import { LayoutTemplate } from 'lucide-react';

import { LabelledColumn } from '@/components/common/labelled-column';
import { cn } from '@/lib/utils';

import type { Template } from '@/lib/types/templates.ts';
import { formatDate } from '@/lib/utils.ts';

/** The same column grid as `DictionaryRow` and `ProjectRow`. */
const COLUMNS_CLASS = 'grid gap-4 grid-cols-[repeat(auto-fit,minmax(160px,1fr))]';

/**
 * One template in the admin list — a toggle that opens its sheet on this same route, as
 * `DictionaryRow` is and for the same reasons (`<button>`, `aria-pressed`).
 */
export function TemplateRow({
  template,
  selected,
  onSelect,
}: {
  template: Template;
  selected: boolean;
  onSelect: () => void;
}) {
  return (
    <button
      type="button"
      aria-pressed={selected}
      onClick={onSelect}
      className={cn(
        'flex cursor-pointer flex-col gap-3 rounded-6 border bg-card px-4 pt-3 pb-4 text-left outline-none',
        'focus-visible:ring-3 focus-visible:ring-ring/50',
        selected ? 'border-blue-400 bg-blue-10' : 'border-neutral-300',
      )}
    >
      <header className="flex h-7 items-center gap-4">
        <LayoutTemplate className="size-5 shrink-0 text-neutral-800" />
        <h3 className="flex-1 truncate text-[14px]/5 font-semibold">{template.name}</h3>
      </header>

      <dl className={COLUMNS_CLASS}>
        <LabelledColumn label="Created">
          <span className="truncate">
            {formatDate(template.createdAt)} by {template.createdBy.displayName}
          </span>
        </LabelledColumn>
        <LabelledColumn label="Last Edited">
          <span className="truncate">
            {formatDate(template.modifiedAt)} by {template.modifiedBy.displayName}
          </span>
        </LabelledColumn>
      </dl>
    </button>
  );
}
