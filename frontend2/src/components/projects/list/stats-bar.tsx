import { Plus } from 'lucide-react';
import { useState } from 'react';

import { Breadcrumbs } from '@/components/layout/breadcrumbs';
import { ProjectFormDialog } from '@/components/projects/project-form-dialog';
import { StatTileGroup } from '@/components/common/stat-tile-group';
import { StatusCountStrip } from '@/components/common/status-count-strip';
import { Button } from '@/components/ui/button';
import { useTotalCounts } from '@/lib/api/projects';
import { useHasPermission } from '@/lib/api/user';

export function StatsBar() {
  const { data } = useTotalCounts();
  // undefined while currentUser resolves — treated as "not yet", so the button never
  // flickers from enabled to disabled.
  const canCreate = useHasPermission('CREATE_PROJECTS') === true;
  const [addOpen, setAddOpen] = useState(false);

  return (
    <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
      <div className="flex items-center gap-4">
        <Breadcrumbs items={[{ label: 'All Projects' }]} className="min-w-0 flex-1" />
        <Button size="lg" className="rounded-md" disabled={!canCreate} onClick={() => setAddOpen(true)}>
          <Plus />
          Add Project
        </Button>
        <ProjectFormDialog open={addOpen} onOpenChange={setAddOpen} />
      </div>
      <div className="flex items-center justify-between gap-4">
        <StatTileGroup
          tiles={[
            { key: 'projects', count: data?.projects },
            { key: 'notebooks', count: data?.notebooks },
            { key: 'experiments', count: data?.experiments },
          ]}
        />
        <StatusCountStrip counts={data?.experimentsByStatus} />
      </div>
    </section>
  );
}
