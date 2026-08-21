import { Plus } from 'lucide-react';

import { StatTileGroup } from '@/components/projects/stat-tile-group';
import { StatusCountStrip } from '@/components/projects/status-count-strip';
import { Button } from '@/components/ui/button';
import { useTotalCounts } from '@/lib/api/projects';

export function StatsBar() {
  const { data } = useTotalCounts();

  return (
    <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
      <div className="flex items-center gap-4">
        <h2 className="flex-1 text-[16px]/6 font-semibold">All Projects</h2>
        {/* TODO: open the create-project form — not part of this screen's design. */}
        <Button size="lg" className="rounded-md">
          <Plus />
          Add Project
        </Button>
      </div>
      <div className="flex items-center justify-between gap-4">
        <StatTileGroup counts={data} />
        <StatusCountStrip counts={data} />
      </div>
    </section>
  );
}
