import { Menu } from '@base-ui/react/menu';
import { ArrowUpDown, ChevronDown, LayoutGrid, LayoutList, Search } from 'lucide-react';
import type { ReactNode } from 'react';

import { Button } from '@/components/ui/button';
import { SegmentedControl } from '@/components/ui/segmented-control';
import { Switch } from '@/components/ui/switch';

import type { CollectionView, SortOrder } from '@/lib/types/common.ts';

const SORT_LABELS: Record<SortOrder, string> = {
  EARLIEST: 'Earliest',
  LATEST: 'Latest',
};

interface ActionBarProps {
  /** Plural entity name, e.g. "projects" — only reaches the screen reader label. */
  entityLabel: string;
  search: string;
  sort: SortOrder;
  createdByMe: boolean;
  view: CollectionView;
  onSearchChange: (search: string) => void;
  onSortChange: (sort: SortOrder) => void;
  onCreatedByMeChange: (createdByMe: boolean) => void;
  onViewChange: (view: CollectionView) => void;
  /** Extra filters or other controls. */
  children?: ReactNode;
}

export function ActionBar({
  entityLabel,
  search,
  sort,
  createdByMe,
  view,
  onSearchChange,
  onSortChange,
  onCreatedByMeChange,
  onViewChange,
  children,
}: ActionBarProps) {
  return (
    <div className="flex h-10 items-center justify-between gap-4">
      <label className="flex h-10 w-70 items-center gap-2 rounded-full border border-blue-10 bg-blue-5 px-4">
        <Search className="size-5 shrink-0 text-neutral-700" />
        <input
          type="search"
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
          placeholder="Search"
          aria-label={`Search ${entityLabel}`}
          className="w-full bg-transparent text-[14px]/6 outline-none placeholder:text-neutral-700"
        />
      </label>

      <div className="flex items-center gap-4">
        <label className="flex items-center gap-2 text-[14px]/6">
          <Switch checked={createdByMe} onCheckedChange={onCreatedByMeChange} />
          My Entities
        </label>

        {children}

        <Menu.Root>
          <Menu.Trigger
            render={
              <Button variant="secondary" size="lg" className="rounded-md">
                <ArrowUpDown />
                <span>
                  Sorting by: <span className="text-blue-400">{SORT_LABELS[sort]}</span>
                </span>
                <ChevronDown />
              </Button>
            }
          />
          <Menu.Portal>
            <Menu.Positioner sideOffset={4} align="end">
              <Menu.Popup className="min-w-40 rounded-md border border-neutral-300 bg-popover p-1 shadow-card outline-none">
                {(Object.keys(SORT_LABELS) as SortOrder[]).map((option) => (
                  <Menu.Item
                    key={option}
                    onClick={() => onSortChange(option)}
                    className="cursor-default rounded-2 px-3 py-2 text-[14px]/6 outline-none data-highlighted:bg-blue-10"
                  >
                    {SORT_LABELS[option]}
                  </Menu.Item>
                ))}
              </Menu.Popup>
            </Menu.Positioner>
          </Menu.Portal>
        </Menu.Root>

        <SegmentedControl<CollectionView>
          value={view}
          onValueChange={onViewChange}
          options={[
            { value: 'grid', label: 'Grid view', icon: <LayoutGrid className="size-5" /> },
            { value: 'list', label: 'List view', icon: <LayoutList className="size-5" /> },
          ]}
        />
      </div>
    </div>
  );
}
