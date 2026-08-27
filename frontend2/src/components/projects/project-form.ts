import { z } from 'zod';

import type { ProjectRequest } from '@/lib/types/projects.ts';

/** ProjectEntity.name is @Size(max = 256); the column is VARCHAR(256). */
export const PROJECT_NAME_MAX_LENGTH = 256;

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

/** An untouched rich-text field serialises to an empty paragraph, not an empty string. */
function isBlankHtml(html: string): boolean {
  return html.replace(/<[^>]*>/g, '').trim() === '';
}

/** Optional fields are dropped when empty so the backend stores null, not "<p></p>". */
export function toProjectRequest(values: ProjectFormValues): ProjectRequest {
  return {
    name: values.name.trim(),
    ...(values.keywords.length > 0 ? { keywords: values.keywords } : {}),
    ...(isBlankHtml(values.literature) ? {} : { literature: values.literature }),
    ...(isBlankHtml(values.description) ? {} : { description: values.description }),
  };
}
