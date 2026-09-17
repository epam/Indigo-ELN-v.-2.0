import { isBlankHtml, richTextEdit } from '@/lib/rich-text';
import { z } from '@/lib/zod';

import type { NotebookDetails, NotebookEditRequest, NotebookRequest } from '@/lib/types/notebooks.ts';
import { NOTEBOOK_NAME_LENGTH } from '@/lib/types/notebooks.ts';

export const notebookNameSchema = z
  .string()
  .trim()
  .min(1, 'Notebook Name is required')
  .regex(new RegExp(`^\\d{${NOTEBOOK_NAME_LENGTH}}$`), `Use ${NOTEBOOK_NAME_LENGTH} digits only`);

export interface NotebookFormValues {
  name: string;
  description: string;
}

/**
 * What the create form starts from. The name is filled in from `/notebooks/next-number` once it
 * arrives — see `NotebookFormDialog` — so this is only what is on screen while that is in flight,
 * and what the dialog resets to when it closes.
 */
export const EMPTY_NOTEBOOK_FORM: NotebookFormValues = {
  name: '',
  description: '',
};

/** The POST body. A blank description is dropped so the backend stores null, not "<p></p>". */
export function toNotebookRequest(values: NotebookFormValues): NotebookRequest {
  return {
    name: values.name.trim(),
    ...(isBlankHtml(values.description) ? {} : { description: values.description }),
  };
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
  const description = richTextEdit(values.description, initial.description);
  if (description) request.description = description.value;

  return request;
}
