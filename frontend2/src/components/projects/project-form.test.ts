import { describe, expect, it } from 'vitest';

import {
  EMPTY_PROJECT_FORM,
  PROJECT_NAME_MAX_LENGTH,
  projectNameSchema,
  toProjectRequest,
} from '@/components/projects/project-form';

describe('toProjectRequest', () => {
  it('sends only the name when nothing else was filled in', () => {
    expect(toProjectRequest({ ...EMPTY_PROJECT_FORM, name: 'Kinase Screening' })).toEqual({
      name: 'Kinase Screening',
    });
  });

  it('trims the name', () => {
    expect(toProjectRequest({ ...EMPTY_PROJECT_FORM, name: '  Padded  ' }).name).toBe('Padded');
  });

  it('drops rich-text fields left as an empty paragraph', () => {
    const request = toProjectRequest({
      ...EMPTY_PROJECT_FORM,
      name: 'P',
      literature: '<p></p>',
      description: '<p><br></p>',
    });
    expect(request).not.toHaveProperty('literature');
    expect(request).not.toHaveProperty('description');
  });

  it('keeps rich text that has actual content', () => {
    const request = toProjectRequest({
      ...EMPTY_PROJECT_FORM,
      name: 'P',
      description: '<p>Add <strong>NaOAc</strong></p>',
    });
    expect(request.description).toBe('<p>Add <strong>NaOAc</strong></p>');
  });

  it('keeps keywords only when some were chosen', () => {
    expect(toProjectRequest({ ...EMPTY_PROJECT_FORM, name: 'P' })).not.toHaveProperty('keywords');
    expect(toProjectRequest({ ...EMPTY_PROJECT_FORM, name: 'P', keywords: ['kinase'] }).keywords).toEqual(['kinase']);
  });
});

describe('projectNameSchema', () => {
  it('rejects a blank name', () => {
    expect(projectNameSchema.safeParse('   ').success).toBe(false);
  });

  it('accepts a name at the column limit but not past it', () => {
    expect(projectNameSchema.safeParse('a'.repeat(PROJECT_NAME_MAX_LENGTH)).success).toBe(true);
    expect(projectNameSchema.safeParse('a'.repeat(PROJECT_NAME_MAX_LENGTH + 1)).success).toBe(false);
  });
});
