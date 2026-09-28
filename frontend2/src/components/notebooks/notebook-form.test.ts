import { describe, expect, it } from 'vitest';

import {
  notebookNameSchema,
  toNotebookEditRequest,
  toNotebookFormValues,
  toNotebookRequest,
} from '@/components/notebooks/notebook-form';
import { NOTEBOOK_NAME_LENGTH } from '@/lib/types/notebooks.ts';
import { makeNotebookDetails } from '@/mocks/fixtures';

describe('notebookNameSchema', () => {
  it('accepts exactly eight digits', () => {
    expect(notebookNameSchema.safeParse('0'.repeat(NOTEBOOK_NAME_LENGTH)).success).toBe(true);
  });

  it('rejects anything shorter or longer', () => {
    expect(notebookNameSchema.safeParse('0'.repeat(NOTEBOOK_NAME_LENGTH - 1)).success).toBe(false);
    expect(notebookNameSchema.safeParse('0'.repeat(NOTEBOOK_NAME_LENGTH + 1)).success).toBe(false);
  });

  /** A notebook is numbered, not named — the Angular form has always enforced digits only. */
  it('rejects a name of the right length that is not all digits', () => {
    expect(notebookNameSchema.safeParse('0000000a').success).toBe(false);
  });

  it('rejects a blank name', () => {
    expect(notebookNameSchema.safeParse('   ').success).toBe(false);
  });
});

describe('toNotebookFormValues', () => {
  it('turns a missing description into the empty string the editor wants', () => {
    expect(toNotebookFormValues(makeNotebookDetails({ description: undefined })).description).toBe('');
  });
});

describe('toNotebookEditRequest', () => {
  const initial = toNotebookFormValues(makeNotebookDetails({ name: '00000001' }));

  it('sends nothing at all when nothing changed', () => {
    expect(toNotebookEditRequest(initial, initial)).toEqual({});
  });

  /** Absent means "leave it alone", so an untouched field must never appear in the body. */
  it('omits the fields that were not touched', () => {
    expect(toNotebookEditRequest({ ...initial, name: '00000002' }, initial)).toEqual({ name: '00000002' });
  });

  /** Dropping the field is how you say "don't touch it", so clearing has to say null. */
  it('sends nothing when an empty description is left alone', () => {
    // Seeded from a null column the form holds `''`, and the editor renders that as `<p></p>`
    // before anything is typed. Compared as strings that reads as a change, and this used to
    // PATCH `description: null` for a field nobody touched.
    const empty = toNotebookFormValues(makeNotebookDetails({ description: undefined }));
    expect(toNotebookEditRequest({ ...empty, description: '<p></p>' }, empty)).toEqual({});
  });

  it('sends null for a description that was emptied', () => {
    expect(toNotebookEditRequest({ ...initial, description: '<p></p>' }, initial).description).toBeNull();
  });

  it('sends the html for a description that was filled in', () => {
    const request = toNotebookEditRequest({ ...initial, description: '<p>Route <em>B</em></p>' }, initial);
    expect(request.description).toBe('<p>Route <em>B</em></p>');
  });

  it('treats a whitespace-only rename as no change', () => {
    expect(toNotebookEditRequest({ ...initial, name: '  00000001  ' }, initial)).toEqual({});
  });
});

describe('toNotebookRequest', () => {
  it('trims the name', () => {
    expect(toNotebookRequest({ name: '  00000004  ', description: '' }).name).toBe('00000004');
  });

  it('omits a description the editor left empty, rather than storing "<p></p>"', () => {
    expect(toNotebookRequest({ name: '00000004', description: '<p></p>' })).toEqual({ name: '00000004' });
  });

  it('sends a description that was written', () => {
    const request = toNotebookRequest({ name: '00000004', description: '<p>Route <em>B</em></p>' });
    expect(request.description).toBe('<p>Route <em>B</em></p>');
  });
});
