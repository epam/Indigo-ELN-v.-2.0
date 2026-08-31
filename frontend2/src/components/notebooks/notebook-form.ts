import {z} from '@/lib/zod';

import type {NotebookDetails, NotebookEditRequest} from '@/lib/types/notebooks.ts';

/** `NOTEBOOK_NAME_LENGTH` in indigo-frontend: a notebook is numbered, never named. */
export const NOTEBOOK_NAME_LENGTH = 8;

export const notebookNameSchema = z
  .string()
  .trim()
  .min(1, 'Notebook Name is required')
  .regex(new RegExp(`^\\d{${NOTEBOOK_NAME_LENGTH}}$`), `Use ${NOTEBOOK_NAME_LENGTH} digits only`);

export interface NotebookFormValues {
  name: string;
  description: string;
}

/** An untouched rich-text field serialises to an empty paragraph, not an empty string. */
function isBlankHtml(html: string): boolean {
  return html.replace(/<[^>]*>/g, '').trim() === '';
}

/** Seeds the edit form from a loaded notebook. A null description becomes the empty string the editor wants. */
export function toNotebookFormValues(notebook: NotebookDetails): NotebookFormValues {
  return {
    name: notebook.name,
    description: notebook.description ?? '',
  };
}

/**
 * The PATCH body, built by diffing against what the form was seeded with.
 *
 * Untouched fields are **omitted**, because `NotebookEditRequest` treats an absent field as
 * "leave it alone" — so an edit never overwrites a value someone else changed in the meantime.
 * A cleared description becomes **null** rather than being dropped, since dropping it is
 * exactly how you say "don't touch it".
 */
export function toNotebookEditRequest(values: NotebookFormValues, initial: NotebookFormValues): NotebookEditRequest {
  const name = values.name.trim();
  const request: NotebookEditRequest = {};

  if (name !== initial.name.trim()) request.name = name;
  if (values.description !== initial.description) {
    request.description = isBlankHtml(values.description) ? null : values.description;
  }

  return request;
}
