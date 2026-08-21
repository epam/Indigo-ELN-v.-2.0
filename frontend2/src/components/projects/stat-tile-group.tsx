import { Briefcase, FileText, NotebookText } from 'lucide-react';

import type { TotalCounts } from '@/lib/types/projects.ts';

export function StatTileGroup({ counts }: { counts: TotalCounts | undefined }) {
  const tiles = [
    { key: 'projects', label: 'Projects', icon: Briefcase, count: counts?.projects },
    { key: 'notebooks', label: 'Notebooks', icon: NotebookText, count: counts?.notebooks },
    { key: 'experiments', label: 'Experiments', icon: FileText, count: counts?.experiments },
  ];

  return (
    <div className="flex gap-px">
      {tiles.map(({ key, label, icon: Icon, count }, index) => (
        <div
          key={key}
          className={`flex w-[120px] flex-col gap-1 bg-blue-10 px-3 py-2 ${
            index === 0 ? 'rounded-l-lg' : ''
          } ${index === tiles.length - 1 ? 'rounded-r-lg' : ''}`}
        >
          <div className="flex items-center gap-2">
            <Icon className="size-5 text-blue-400" />
            <span className="text-[18px]/7 font-semibold">{count ?? '—'}</span>
          </div>
          <span className="text-[14px]/6 text-neutral-800">{label}</span>
        </div>
      ))}
    </div>
  );
}
