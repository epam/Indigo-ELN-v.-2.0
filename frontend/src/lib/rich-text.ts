/**
 * What a rich-text field's stored value is worth, and whether it changed.
 *
 * Every rich-text field in the app — project and notebook descriptions, project literature, the
 * experiment description — reads and writes HTML strings produced by `RichTextEditor`, against
 * backend columns that are nullable TEXT. Three copies of the "is this empty" test had grown up
 * around that, two of them missing cases; this is the one.
 */

/**
 * Whether an HTML fragment carries nothing a reader would see.
 *
 * Tiptap never yields an empty string: an emptied editor serialises to `<p></p>`, and pressing
 * Enter first leaves `<p></p><p></p>`. So a field that looks empty is not `''`, and comparing it
 * against a backend `null` needs this rather than a truthiness check.
 *
 * An `<img>` counts as content even though it contributes no text — the older copies of this in
 * the project and notebook forms did not, so an image-only description was treated as blank and
 * silently dropped on save.
 */
export function isBlankHtml(html: string | undefined): boolean {
  if (!html) return true;
  if (/<img\b/i.test(html)) return false;
  return (
    html
      .replace(/<[^>]*>/g, '')
      // `&nbsp;` is what a deliberately blank paragraph usually leaves behind.
      .replace(/&nbsp;/gi, ' ')
      .trim() === ''
  );
}

/**
 * What to send for a rich-text field, or `null` when it did not really change.
 *
 * The wrapper object is what separates the two kinds of nothing, and it is worth the allocation:
 * a bare `string | null | undefined` return would make `if (value)` — the obvious thing to write
 * — silently skip a legitimate *clear*, since `null` is falsy too.
 *
 * - `null` from this function means **send nothing**. Every edit request wraps its fields in
 *   `JsonNullable` with `@JsonInclude(NON_ABSENT)`, so an absent field means "leave it alone",
 *   which is also what stops an edit clobbering a value someone else changed meanwhile.
 * - `{ value: null }` means **clear the column**, which is what an emptied editor should do
 *   rather than storing `<p></p>` as though it were content.
 *
 * Both sides are reduced to "is there anything here" before being compared, so a field that was
 * empty and still is reports no change however each side happens to spell it — `undefined` from
 * the server, `''` in a freshly seeded form, `<p></p>` once the editor has rendered it.
 *
 * **`baseline` must be what the field held when the user last entered it, not what the server
 * last sent.** Tiptap rewrites stored HTML into its own canonical form as it loads — plain text
 * gains a `<p>`, a `<div>` becomes a `<p>`, newlines between tags vanish — so the stored string
 * is not a fair comparison. See `ExperimentDescriptionPanel`, which snapshots on focus.
 */
export function richTextEdit(draft: string, baseline: string | undefined): { value: string | null } | null {
  const draftBlank = isBlankHtml(draft);
  if (draftBlank && isBlankHtml(baseline)) return null;
  if (!draftBlank && draft === baseline) return null;
  return { value: draftBlank ? null : draft };
}
