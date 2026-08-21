import { Link } from '@tanstack/react-router';
import { Briefcase } from 'lucide-react';

import { AvatarStack } from '@/components/projects/avatar-stack';

import type { Project } from '@/lib/types/projects.ts';
import { formatDate } from '@/lib/utils.ts';

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex items-center justify-between gap-4">
      <dt className="text-[14px]/6 text-neutral-800">{label}</dt>
      <dd className="flex items-center gap-2 text-[14px]/6">{children}</dd>
    </div>
  );
}

export function ProjectCard({ project }: { project: Project }) {
  const openCount = project.experimentCountByStatus.OPEN ?? 0;

  return (
    <Link
      to="/projects/$id"
      params={{ id: project.id }}
      className="flex cursor-pointer flex-col gap-3 rounded-6 border border-neutral-300 bg-card px-4 pt-3 pb-4"
    >
      <header className="flex h-7 items-center gap-4">
        <Briefcase className="size-5 shrink-0 text-neutral-800" />
        <h3 className="flex-1 truncate text-[14px]/5 font-semibold">{project.name}</h3>
        <AvatarStack acl={project.acl} aclCount={project.aclCount} />
      </header>

      <dl className="flex flex-col gap-1">
        <Row label="Notebook">
          <span className="text-[16px]/6 font-semibold">{project.notebookCount}</span>
        </Row>
        <Row label="Experiments">
          <span className="text-[16px]/6 font-semibold text-blue-400">{openCount}</span>
          <span className="h-4 w-px bg-neutral-300" />
          <span className="text-[16px]/6 font-semibold text-neutral-1000">{project.experimentCount}</span>
        </Row>
        <Row label="Last Edited">
          {formatDate(project.modifiedAt)} {project.modifiedBy.displayName}
        </Row>
        <Row label="Created">
          {formatDate(project.createdAt)} {project.createdBy.displayName}
        </Row>
      </dl>
    </Link>
  );
}
