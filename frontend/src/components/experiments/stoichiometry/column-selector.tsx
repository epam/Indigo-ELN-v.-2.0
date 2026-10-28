import { Columns3 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Menu, MenuCheckboxItem, MenuContent, MenuItem, MenuTrigger } from '@/components/ui/menu';

import type { HideableColumn } from '@/lib/hooks/experiments/use-hidden-columns';

/**
 * The Column Selector in a stoichiometry table's toolbar: one tick per column the user may switch
 * off, and Reset to put them all back.
 *
 * A tick takes effect at once and the menu stays open — `MenuCheckboxItem` does not close on
 * click — so several columns can be switched in one visit and the table shows each change behind
 * the menu. That is the Angular app's behaviour and the experiment list's Status filter's; an
 * Apply step would only hide the result until the end.
 */
export function ColumnSelector({
  columns,
  hidden,
  onToggle,
  onReset,
}: {
  columns: readonly HideableColumn[];
  hidden: ReadonlySet<string>;
  onToggle: (id: string, visible: boolean) => void;
  onReset: () => void;
}) {
  return (
    <Menu>
      <MenuTrigger
        render={
          <Button variant="ghost" size="icon" aria-label="Choose columns" title="Choose columns">
            <Columns3 />
          </Button>
        }
      />
      <MenuContent className="max-h-[60vh] overflow-y-auto">
        {columns.map((column) => (
          <MenuCheckboxItem
            key={column.id}
            checked={!hidden.has(column.id)}
            onCheckedChange={(checked) => onToggle(column.id, checked)}
          >
            {column.header}
          </MenuCheckboxItem>
        ))}
        <MenuItem
          disabled={hidden.size === 0}
          onClick={onReset}
          className="mt-1 rounded-none border-t border-neutral-300 text-blue-400"
        >
          Reset to default
        </MenuItem>
      </MenuContent>
    </Menu>
  );
}
