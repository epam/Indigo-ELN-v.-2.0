/**
 * A stored rich-text field, read-only — the same HTML strings `RichTextEditor` writes, against
 * the backend's unbounded TEXT columns. `.tiptap-content` is where the mark styles live (the
 * editor emits bare tags with no classes), so read and write views render identically.
 *
 * The HTML comes from the backend, which is the same trust boundary every other field on these
 * pages sits behind.
 */
export function RichText({ html }: { html: string | undefined }) {
  if (!html?.trim()) return <p className="text-neutral-1000">-</p>;
  return <div className="tiptap-content text-neutral-1000" dangerouslySetInnerHTML={{ __html: html }} />;
}
