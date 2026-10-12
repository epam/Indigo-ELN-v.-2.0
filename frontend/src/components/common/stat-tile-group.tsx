import { Briefcase, FileText, NotebookText } from 'lucide-react';

import { Skeleton } from '@/components/ui/skeleton';

/**
 * Label and icon are fixed per entity; only which tiles appear and what they count varies —
 * the projects page shows all three, a project shows the two it contains.
 */
const TILES = {
  projects: { label: 'Projects', icon: Briefcase },
  notebooks: { label: 'Notebooks', icon: NotebookText },
  experiments: { label: 'Experiments', icon: FileText },
} as const;

export type StatTileKey = keyof typeof TILES;

/** `count: undefined` is "still loading" — the tile keeps its size and shows a skeleton. */
export interface StatTile {
  key: StatTileKey;
  count: number | undefined;
}

export function StatTileGroup({ tiles }: { tiles: StatTile[] }) {
  return (
    <div className="flex gap-px">
      {tiles.map(({ key, count }, index) => {
        const { label, icon: Icon } = TILES[key];

        return (
          <div
            key={key}
            className={`flex w-30 flex-col gap-1 bg-blue-10 px-3 py-2 ${
              index === 0 ? 'rounded-l-lg' : ''
            } ${index === tiles.length - 1 ? 'rounded-r-lg' : ''}`}
          >
            <div className="flex items-center gap-2">
              <Icon className="size-5 text-blue-400" />
              {/* my-1 keeps the 28px line box of the real count, so the tile never resizes. */}
              {count === undefined ? (
                <Skeleton className="my-1 h-5 w-10" />
              ) : (
                <span className="text-[18px]/7 font-semibold">{count}</span>
              )}
            </div>
            <span className="text-[14px]/6 text-neutral-800">{label}</span>
          </div>
        );
      })}
    </div>
  );
}
