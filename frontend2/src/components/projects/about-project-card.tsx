import {Pencil} from 'lucide-react';
import type {ReactNode} from 'react';
import {useState} from 'react';

import {AttachmentList} from '@/components/projects/attachment-list';
import {ProjectFormDialog} from '@/components/projects/project-form-dialog';
import {Button} from '@/components/ui/button';

import type {ProjectDetails} from '@/lib/types/projects.ts';

function Section({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex flex-col gap-2">
      <h3 className="font-semibold text-neutral-800">{label}</h3>
      {children}
    </div>
  );
}

/**
 * Literature and description are HTML — the same strings `RichTextEditor` writes, against the
 * backend's unbounded TEXT columns. `.tiptap-content` is where the mark styles live (the editor
 * emits bare tags with no classes), so read and write views render identically.
 */
function RichText({ html }: { html: string | undefined }) {
  if (!html?.trim()) return <p className="text-neutral-1000">-</p>;
  return <div className="tiptap-content text-neutral-1000" dangerouslySetInnerHTML={{ __html: html }} />;
}

export function AboutProjectCard({ project }: { project: ProjectDetails }) {
  const [editOpen, setEditOpen] = useState(false);
  const canEdit = project.currentPermissions.includes('EDIT_PROJECTS');

  return (
    <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
      <div className="flex items-center justify-between gap-4 border-b border-neutral-300 pb-3">
        <h2 className="text-[16px]/6 font-semibold">About Project</h2>
        {canEdit && (
          <>
            <Button variant="outline" size="icon" aria-label="Edit project" onClick={() => setEditOpen(true)}>
              <Pencil className="text-blue-400" />
            </Button>
            <ProjectFormDialog open={editOpen} onOpenChange={setEditOpen} project={project} />
          </>
        )}
      </div>

      <div className="flex flex-col gap-6 text-[14px]/6">
        <Section label="Project Code & Name">
          <p className="text-neutral-1000">{project.name}</p>
        </Section>

        <Section label="Project Keywords">
          {project.keywords.length > 0 ? (
            <ul className="flex flex-wrap gap-2">
              {project.keywords.map((keyword) => (
                <li key={keyword} className="rounded-full bg-blue-10 px-3 py-1 text-[12px]/5 text-neutral-1000">
                  {keyword}
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-neutral-1000">-</p>
          )}
        </Section>

        <Section label="Literature">
          <RichText html={project.literature} />
        </Section>

        <Section label="Project Description">
          <RichText html={project.description} />
        </Section>

        <AttachmentList projectId={project.id} attachments={project.attachments} canEdit={canEdit} />
      </div>
    </section>
  );
}
