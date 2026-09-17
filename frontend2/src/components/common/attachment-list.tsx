import { File as FileIcon, FileImage, FileSpreadsheet, FileText, Paperclip, Trash2 } from 'lucide-react';
import { useRef } from 'react';

import { Button } from '@/components/ui/button';
import { useDownload } from '@/lib/hooks/use-download';
import { formatBytes, formatDate } from '@/lib/utils';

import type { UseMutationResult } from '@tanstack/react-query';
import type { Attachment } from '@/lib/types/common.ts';

/**
 * The three calls an attachment list makes, passed in rather than imported: projects and
 * notebooks declare identical endpoints under different prefixes, and this way the component
 * never learns which entity it is attached to. Each caller owns its own cache patching.
 */
export interface AttachmentActions {
  upload: UseMutationResult<Attachment[], Error, File[]>;
  remove: UseMutationResult<void, Error, string>;
  /** Resolved by `useDownload`, which swallows the error apiFetch has already toasted. */
  download: (attachment: Attachment) => Promise<void>;
}

/** Same extension groups indigo-frontend's attachment component used. */
function AttachmentIcon({ name }: { name: string }) {
  const className = 'size-4 shrink-0';
  switch (name.split('.').pop()?.toLowerCase()) {
    case 'pdf':
    case 'doc':
    case 'docx':
      return <FileText className={className} />;
    case 'png':
    case 'jpg':
    case 'jpeg':
      return <FileImage className={className} />;
    case 'xls':
    case 'xlsx':
      return <FileSpreadsheet className={className} />;
    default:
      return <FileIcon className={className} />;
  }
}

function AttachmentRow({
  attachment,
  onDownload,
  canEdit,
  onDelete,
  deleting,
}: {
  attachment: Attachment;
  onDownload: () => Promise<void>;
  canEdit: boolean;
  onDelete: () => void;
  deleting: boolean;
}) {
  // Per row, not per list: each file downloads on its own and disables only its own link.
  const { download, downloading } = useDownload();

  return (
    <li className="grid grid-cols-[minmax(0,2fr)_minmax(0,1.5fr)_5rem_minmax(0,1fr)_auto] items-center gap-4 rounded-md border border-neutral-300 px-3 py-2 text-[14px]/6">
      <button
        type="button"
        onClick={() => void download(onDownload)}
        disabled={downloading}
        className="flex min-w-0 items-center gap-2 text-left text-blue-400 hover:underline disabled:opacity-60"
      >
        <AttachmentIcon name={attachment.name} />
        <span className="truncate">{attachment.name}</span>
      </button>
      <span className="truncate text-neutral-800">by {attachment.createdBy.displayName}</span>
      <span className="text-neutral-800">{formatBytes(attachment.size)}</span>
      <span className="truncate text-neutral-800">{formatDate(attachment.createdAt)}</span>
      {canEdit ? (
        <Button
          variant="ghost"
          size="icon-sm"
          aria-label={`Delete ${attachment.name}`}
          loading={deleting}
          onClick={onDelete}
          className="text-red-200 hover:bg-red-10 hover:text-red-200"
        >
          <Trash2 />
        </Button>
      ) : (
        // Keeps the column, so rows line up regardless of canEdit.
        <span className="size-8" />
      )}
    </li>
  );
}

export function AttachmentList({
  attachments,
  canEdit,
  heading = 'Attachments',
  actions: { upload, remove, download },
}: {
  attachments: Attachment[];
  canEdit: boolean;
  /**
   * `null` drops the heading, for a surface that already names itself — the experiment screen
   * renders this as the whole body of a card titled *Attachments*, and would say it twice.
   */
  heading?: string | null;
  actions: AttachmentActions;
}) {
  const inputRef = useRef<HTMLInputElement>(null);

  return (
    <div className="flex flex-col gap-2">
      {heading !== null && <h3 className="font-semibold text-neutral-800">{heading}</h3>}

      {attachments.length > 0 && (
        <ul className="flex flex-col gap-2">
          {attachments.map((attachment) => (
            <AttachmentRow
              key={attachment.id}
              attachment={attachment}
              onDownload={() => download(attachment)}
              canEdit={canEdit}
              deleting={remove.isPending && remove.variables === attachment.id}
              onDelete={() => remove.mutate(attachment.id)}
            />
          ))}
        </ul>
      )}

      {canEdit && (
        <>
          <input
            ref={inputRef}
            type="file"
            multiple
            className="hidden"
            onChange={(event) => {
              const files = Array.from(event.target.files ?? []);
              // Cleared first, so picking the same file twice in a row still fires onChange.
              event.target.value = '';
              if (files.length > 0) upload.mutate(files);
            }}
          />
          <Button
            variant="outline"
            loading={upload.isPending}
            onClick={() => inputRef.current?.click()}
            className="w-full border-dashed text-blue-400"
          >
            <Paperclip />
            Attach File
          </Button>
        </>
      )}
    </div>
  );
}
