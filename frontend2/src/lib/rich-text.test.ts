import { describe, expect, it } from 'vitest';

import { isBlankHtml, richTextEdit } from '@/lib/rich-text';

describe('isBlankHtml', () => {
  it('treats undefined and the empty string as blank', () => {
    expect(isBlankHtml(undefined)).toBe(true);
    expect(isBlankHtml('')).toBe(true);
  });

  /** What Tiptap serialises an emptied editor to. */
  it('treats an empty paragraph as blank', () => {
    expect(isBlankHtml('<p></p>')).toBe(true);
    expect(isBlankHtml('<p><br></p>')).toBe(true);
    expect(isBlankHtml('<p></p><p></p>')).toBe(true);
    expect(isBlankHtml('<p>&nbsp;</p>')).toBe(true);
  });

  it('treats text as content, however it is marked up', () => {
    expect(isBlankHtml('<p><strong>Heat</strong> to 40 °C</p>')).toBe(false);
  });

  /** An image contributes no text but is plainly content. */
  it('treats an image as content', () => {
    expect(isBlankHtml('<p><img src="data:image/png;base64,AAA"></p>')).toBe(false);
  });
});

describe('richTextEdit', () => {
  it('sends nothing when the text is unchanged', () => {
    expect(richTextEdit('<p>Same</p>', '<p>Same</p>')).toBeNull();
  });

  /** The whole point: focusing and leaving an empty editor must not cost a request. */
  it('sends nothing when an empty editor is left untouched', () => {
    expect(richTextEdit('<p></p>', undefined)).toBeNull();
    expect(richTextEdit('<p><br></p>', '')).toBeNull();
  });

  it('sends the new HTML when the text changed', () => {
    expect(richTextEdit('<p>New</p>', '<p>Old</p>')).toEqual({ value: '<p>New</p>' });
  });

  it('sends the first text typed into an empty description', () => {
    expect(richTextEdit('<p>First</p>', undefined)).toEqual({ value: '<p>First</p>' });
  });

  /**
   * The case the project and notebook edit dialogs hit: the form is seeded with `''` from a null
   * column, and the editor renders that as `<p></p>` before anything is typed. Compared as
   * strings that reads as a change, and both dialogs used to PATCH `description: null` for a
   * field nobody touched.
   */
  it('sends nothing when an empty field is spelled differently on each side', () => {
    expect(richTextEdit('<p></p>', '')).toBeNull();
    expect(richTextEdit('<p><br></p>', undefined)).toBeNull();
    expect(richTextEdit('', '<p></p>')).toBeNull();
  });

  /** null clears the column, rather than storing `<p></p>` as though it were content. */
  it('clears the field when the text was deleted', () => {
    expect(richTextEdit('<p></p>', '<p>Old</p>')).toEqual({ value: null });
  });
});
