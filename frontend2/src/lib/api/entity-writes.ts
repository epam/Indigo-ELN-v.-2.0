import { useMutation, useQueryClient } from '@tanstack/react-query';
import type { MutationKey, QueryClient, QueryKey } from '@tanstack/react-query';

import { apiDownload, apiFetch } from '@/lib/api';

import type { AccessForm, ACLEntry, Attachment } from '@/lib/types/common.ts';

/**
 * What every detail-entity write shares, the way `collections.ts` holds what every paged list
 * shares. Projects, notebooks and experiments expose the same two sub-resources under the same
 * paths — `{base}/{id}/attachments` and `{base}/{id}/access` — and answer them the same way, so
 * the three copies differed only in the prefix, the detail key, and (experiments only) the
 * mutation scope.
 */
export interface EntityWriteTarget {
  /** The collection path, without a trailing slash — e.g. `/api/eln/projects`. */
  basePath: string;
  /** Where the entity's detail sits in the cache, so a response can be patched into it. */
  detailKey: (id: string) => QueryKey;
  /**
   * Extra mutation options, if this entity serialises its writes. Only experiments do:
   * `experimentWrite(id)` supplies a `mutationKey` to count pending writes by and a `scope`
   * that makes TanStack Query run them one at a time.
   */
  writeOptions?: (id: string) => { mutationKey?: MutationKey; scope?: { id: string } };
}

/** Patches one field of the cached detail, leaving the rest of the entity untouched. */
function patchDetail<T>(queryClient: QueryClient, target: EntityWriteTarget, id: string, patch: (entity: T) => T) {
  queryClient.setQueryData<T>(target.detailKey(id), (entity) => (entity ? patch(entity) : entity));
}

/** Returns the entity's full attachment list, not just the new entries. */
function uploadAttachment(target: EntityWriteTarget, id: string, file: File): Promise<Attachment[]> {
  const formData = new FormData();
  formData.append('file', file, file.name);
  return apiFetch<Attachment[]>(`${target.basePath}/${id}/attachments`, { method: 'POST', formData });
}

function deleteAttachment(target: EntityWriteTarget, id: string, attachmentId: string): Promise<void> {
  return apiFetch<void>(`${target.basePath}/${id}/attachments/${attachmentId}`, { method: 'DELETE' });
}

/**
 * Files are uploaded one at a time: the endpoint takes a single `file` part, and each response
 * carries the full list, so a parallel upload would race and the last response home would drop
 * the others. `attachments` is read from the final response rather than accumulated.
 *
 * Where the target serialises its writes, that scope covers this mutation against *other* writes;
 * the loop below serialises the files of one upload against each other.
 */
function useUploadAttachments<T extends { attachments: Attachment[] }>(target: EntityWriteTarget, id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    ...target.writeOptions?.(id),
    mutationFn: async (files: File[]) => {
      let attachments: Attachment[] = [];
      for (const file of files) {
        attachments = await uploadAttachment(target, id, file);
      }
      return attachments;
    },
    onSuccess: (attachments) => patchDetail<T>(queryClient, target, id, (entity) => ({ ...entity, attachments })),
  });
}

function useDeleteAttachment<T extends { attachments: Attachment[] }>(target: EntityWriteTarget, id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    ...target.writeOptions?.(id),
    mutationFn: (attachmentId: string) => deleteAttachment(target, id, attachmentId),
    onSuccess: (_result, attachmentId) =>
      patchDetail<T>(queryClient, target, id, (entity) => ({
        ...entity,
        attachments: entity.attachments.filter((attachment) => attachment.id !== attachmentId),
      })),
  });
}

/**
 * The three calls `AttachmentList` needs, bound to one entity. Returned as a plain object so the
 * component stays ignorant of which entity it is attached to; the shape is checked structurally
 * against `AttachmentActions` where it is passed in.
 *
 * The download endpoint sets `Content-Disposition` from the same name the DTO carries, so the
 * fallback filename matters only if that header is ever stripped in transit.
 */
export function useEntityAttachments<T extends { attachments: Attachment[] }>(target: EntityWriteTarget, id: string) {
  const upload = useUploadAttachments<T>(target, id);
  const remove = useDeleteAttachment<T>(target, id);

  return {
    upload,
    remove,
    download: (attachment: Attachment) =>
      apiDownload(`${target.basePath}/${id}/attachments/${attachment.id}`, attachment.name),
  };
}

function updateAccess(target: EntityWriteTarget, id: string, updates: AccessForm[]): Promise<ACLEntry[]> {
  return apiFetch<ACLEntry[]>(`${target.basePath}/${id}/access`, {
    method: 'POST',
    json: updates,
  });
}

/**
 * `ACLService.update*ACL` upserts entry by entry, so only what changed needs sending. The response
 * is the recomputed ACL for the whole entity — inherited entries included — so it replaces `acl`
 * wholesale rather than being merged in.
 *
 * The detail is the one copy that has to be written. The Team surfaces read the detail's `acl` and
 * nothing else: the list DTO carries `shortACL`, capped at three, and an entity opened by direct
 * link has no list loaded at all.
 */
export function useUpdateEntityAccess<T extends { acl: ACLEntry[] }>(target: EntityWriteTarget, id: string) {
  const queryClient = useQueryClient();

  return useMutation({
    ...target.writeOptions?.(id),
    mutationFn: (updates: AccessForm[]) => updateAccess(target, id, updates),
    onSuccess: (acl) => patchDetail<T>(queryClient, target, id, (entity) => ({ ...entity, acl })),
  });
}
