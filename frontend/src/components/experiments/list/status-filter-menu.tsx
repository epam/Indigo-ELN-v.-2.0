import { ChevronDown } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Menu, MenuCheckboxItem, MenuContent, MenuTrigger } from '@/components/ui/menu';
import { EXPERIMENT_STATUS_LABELS, EXPERIMENT_STATUSES, type ExperimentStatus } from '@/lib/types/experiments.ts';

/**
 * The `Status` filter of a notebook's experiment list, sitting in `ActionBar`'s children slot.
 *
 * `/notebooks/{id}/experiments` takes a repeatable `status` param and treats an empty list as
 * no filter at all, so "nothing ticked" and "everything ticked" are the same query — which is
 * why there is no explicit All entry to tick.
 */
export function StatusFilterMenu({
  value,
  onValueChange,
}: {
  value: ExperimentStatus[];
  onValueChange: (value: ExperimentStatus[]) => void;
}) {
  const selected = new Set(value);

  // Rebuilt from EXPERIMENT_STATUSES rather than appended to, so the order in the URL is the
  // display order however the boxes were ticked.
  const toggle = (status: ExperimentStatus, checked: boolean) => {
    const next = new Set(selected);
    if (checked) next.add(status);
    else next.delete(status);
    onValueChange(EXPERIMENT_STATUSES.filter((option) => next.has(option)));
  };

  return (
    <Menu>
      <MenuTrigger
        render={
          <Button variant="secondary" size="lg" className="rounded-md">
            {/*
              The space belongs to the outer text node, not the inner span — a leading space
              inside the span is stripped when the accessible name is computed, leaving
              "Status(2)". Same shape as ActionBar's sort trigger.
            */}
            <span>Status {selected.size > 0 && <span className="text-blue-400">({selected.size})</span>}</span>
            <ChevronDown />
          </Button>
        }
      />
      <MenuContent>
        {EXPERIMENT_STATUSES.map((status) => (
          <MenuCheckboxItem
            key={status}
            checked={selected.has(status)}
            onCheckedChange={(checked) => toggle(status, checked)}
          >
            {EXPERIMENT_STATUS_LABELS[status]}
          </MenuCheckboxItem>
        ))}
      </MenuContent>
    </Menu>
  );
}
