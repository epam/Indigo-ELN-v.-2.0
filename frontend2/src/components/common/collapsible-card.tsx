import { ChevronDown } from 'lucide-react';
import type { ReactNode } from 'react';
import { useState } from 'react';

import { Collapsible, CollapsiblePanel, CollapsibleTrigger } from '@/components/ui/collapsible';
import { cn } from '@/lib/utils';

/**
 * A titled card whose body folds away — the shell every experiment template component renders in.
 *
 * `actions` are siblings of the trigger, never children of it: nesting a button inside the
 * trigger would make every click on it toggle the card as well as run the action.
 *
 * Open state is held here rather than taken as a prop, matching `AdvancedSearch`: the chevron
 * needs to know, and no caller has yet had a reason to drive it.
 */
export function CollapsibleCard({
  title,
  titleId,
  actions,
  defaultOpen = true,
  className,
  children,
}: {
  title: string;
  /**
   * Puts an id on the heading so a control in the body can name itself after it — the rich-text
   * description does, rather than repeating the card's own title in a hidden label.
   */
  titleId?: string;
  actions?: ReactNode;
  defaultOpen?: boolean;
  className?: string;
  children: ReactNode;
}) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <Collapsible open={open} onOpenChange={setOpen} className={cn('rounded-6 bg-card p-4 shadow-card', className)}>
      <div className="flex items-center gap-4">
        <CollapsibleTrigger className="min-w-0 flex-1 rounded-2">
          <ChevronDown
            aria-hidden
            className={cn('size-5 shrink-0 text-neutral-700 transition-transform duration-150', open && 'rotate-180')}
          />
          <h2 id={titleId} className="truncate text-[16px]/6 font-semibold text-neutral-1000">
            {title}
          </h2>
        </CollapsibleTrigger>
        {actions && <div className="flex shrink-0 items-center gap-2">{actions}</div>}
      </div>

      <CollapsiblePanel>
        <div className="pt-4">{children}</div>
      </CollapsiblePanel>
    </Collapsible>
  );
}
