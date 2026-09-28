import { isBlankHtml, richTextEdit } from '@/lib/rich-text';
import { z } from '@/lib/zod';

import type { ProjectDetails, ProjectEditRequest, ProjectRequest } from '@/lib/types/projects.ts';
import { PROJECT_NAME_MAX_LENGTH } from '@/lib/types/projects.ts';

export const projectNameSchema = z
  .string()
  .trim()
  .min(1, 'Project Name is required')
  .max(PROJECT_NAME_MAX_LENGTH, `Use ${PROJECT_NAME_MAX_LENGTH} characters at most`);

export interface ProjectFormValues {
  name: string;
  keywords: string[];
  literature: string;
  description: string;
}

export const EMPTY_PROJECT_FORM: ProjectFormValues = {
  name: '',
  keywords: [],
  literature: '',
  description: '',
};

/** Optional fields are dropped when empty so the backend stores null, not "<p></p>". */
export function toProjectRequest(values: ProjectFormValues): ProjectRequest {
  return {
    name: values.name.trim(),
    ...(values.keywords.length > 0 ? { keywords: values.keywords } : {}),
    ...(isBlankHtml(values.literature) ? {} : { literature: values.literature }),
    ...(isBlankHtml(values.description) ? {} : { description: values.description }),
  };
}

/** Seeds the edit form from a loaded project. Nullable fields become the empty string the editors want. */
export function toProjectFormValues(project: ProjectDetails): ProjectFormValues {
  return {
    name: project.name,
    keywords: project.keywords,
    literature: project.literature ?? '',
    description: project.description ?? '',
  };
}

function sameKeywords(a: string[], b: string[]): boolean {
  return a.length === b.length && a.every((keyword, index) => keyword === b[index]);
}

/**
 * The PATCH body, built by diffing against what the form was seeded with.
 *
 * Two things separate this from `toProjectRequest`. Untouched fields are **omitted**, because
 * `ProjectEditRequest` treats an absent field as "leave it alone" — so an edit never overwrites
 * a value someone else changed in the meantime. And a cleared rich-text field becomes **null**
 * rather than being dropped, since dropping it is exactly how you say "don't touch it": the
 * create path can drop a blank because there is nothing there yet to clear.
 */
export function toProjectEditRequest(values: ProjectFormValues, initial: ProjectFormValues): ProjectEditRequest {
  const name = values.name.trim();
  const request: ProjectEditRequest = {};

  if (name !== initial.name.trim()) request.name = name;
  if (!sameKeywords(values.keywords, initial.keywords)) request.keywords = values.keywords;
  const literature = richTextEdit(values.literature, initial.literature);
  if (literature) request.literature = literature.value;
  const description = richTextEdit(values.description, initial.description);
  if (description) request.description = description.value;

  return request;
}
