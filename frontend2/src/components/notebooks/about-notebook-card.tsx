import { Pencil } from 'lucide-react';
import type { ReactNode } from 'react';
import { useState } from 'react';

import { AttachmentList } from '@/components/common/attachment-list';
import { RichText } from '@/components/common/rich-text';
import { NotebookFormDialog } from '@/components/notebooks/notebook-form-dialog';
import { Button } from '@/components/ui/button';
import { useNotebookAttachments } from '@/lib/api/notebooks';

import type { NotebookDetails } from '@/lib/types/notebooks.ts';

function Section({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex flex-col gap-2">
      <h3 className="font-semibold text-neutral-800">{label}</h3>
      {children}
    </div>
  );
}

export function AboutNotebookCard({ notebook }: { notebook: NotebookDetails }) {
  const [editOpen, setEditOpen] = useState(false);
  const canEdit = notebook.currentPermissions.includes('EDIT_NOTEBOOKS');
  const attachments = useNotebookAttachments(notebook.id);

  return (
    <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
      <div className="flex items-center justify-between gap-4 border-b border-neutral-300 pb-3">
        <h2 className="text-[16px]/6 font-semibold">About Notebook</h2>
        {canEdit && (
          <>
            <Button variant="outline" size="icon" aria-label="Edit notebook" onClick={() => setEditOpen(true)}>
              <Pencil className="text-blue-400" />
            </Button>
            <NotebookFormDialog open={editOpen} onOpenChange={setEditOpen} notebook={notebook} />
          </>
        )}
      </div>

      <div className="flex flex-col gap-6 text-[14px]/6">
        <Section label="Notebook Name">
          <p className="text-neutral-1000">{notebook.name}</p>
        </Section>

        <Section label="Notebook Description">
          <RichText html={notebook.description} />
        </Section>

        <AttachmentList attachments={notebook.attachments} canEdit={canEdit} actions={attachments} />
      </div>
    </section>
  );
}
