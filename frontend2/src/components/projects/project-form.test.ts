import { describe, expect, it } from 'vitest';

import {
  EMPTY_PROJECT_FORM,
  projectNameSchema,
  toProjectEditRequest,
  toProjectFormValues,
  toProjectRequest,
} from '@/components/projects/project-form';
import { PROJECT_NAME_MAX_LENGTH } from '@/lib/types/projects.ts';
import { makeProjectDetails } from '@/mocks/fixtures';

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

describe('toProjectFormValues', () => {
  it('turns the nullable fields into the empty strings the editors want', () => {
    const values = toProjectFormValues(makeProjectDetails({ literature: undefined, description: undefined }));
    expect(values.literature).toBe('');
    expect(values.description).toBe('');
  });
});

describe('toProjectEditRequest', () => {
  const initial = toProjectFormValues(makeProjectDetails({ name: 'Kinases', keywords: ['kinase'] }));

  it('sends nothing at all when nothing changed', () => {
    expect(toProjectEditRequest(initial, initial)).toEqual({});
  });

  /** Absent means "leave it alone", so an untouched field must never appear in the body. */
  it('omits the fields that were not touched', () => {
    const request = toProjectEditRequest({ ...initial, name: 'Kinases II' }, initial);
    expect(request).toEqual({ name: 'Kinases II' });
  });

  it('sends nothing when empty rich-text fields are left alone', () => {
    // Seeded from null columns the form holds `''`, and the editors render those as `<p></p>`
    // before anything is typed. Compared as strings that reads as a change, and this used to
    // PATCH `literature: null` and `description: null` for fields nobody touched.
    const empty = toProjectFormValues(makeProjectDetails({ literature: undefined, description: undefined }));
    expect(toProjectEditRequest({ ...empty, literature: '<p></p>', description: '<p><br></p>' }, empty)).toEqual({});
  });

  /** Where the create path drops a blank, the edit path has to say null or nothing is cleared. */
  it('sends null for a rich-text field that was emptied', () => {
    const request = toProjectEditRequest({ ...initial, description: '<p></p>' }, initial);
    expect(request.description).toBeNull();
  });

  it('sends the html for a rich-text field that was filled in', () => {
    const request = toProjectEditRequest({ ...initial, literature: '<p>Smith 2025</p>' }, initial);
    expect(request.literature).toBe('<p>Smith 2025</p>');
  });

  it('treats a whitespace-only rename as no change', () => {
    expect(toProjectEditRequest({ ...initial, name: '  Kinases  ' }, initial)).toEqual({});
  });

  it('sends the whole keyword list once any of it changed, and an empty one to clear it', () => {
    expect(toProjectEditRequest({ ...initial, keywords: ['kinase', 'assay'] }, initial).keywords).toEqual([
      'kinase',
      'assay',
    ]);
    expect(toProjectEditRequest({ ...initial, keywords: [] }, initial).keywords).toEqual([]);
  });
});
