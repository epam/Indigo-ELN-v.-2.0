import type { TemplateTab } from '@/lib/types/templates.ts';

/**
 * A tab name as it appears in `?tab=`. Anything that is not a letter or a digit collapses to a
 * single dash, so `Previous Versions` addresses as `previous-versions`.
 */
export function tabSlug(name: string): string {
  return name
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

/**
 * One slug per tab, in order, de-duplicated by index. Tab names come from template data and
 * nothing stops two of them slugging alike — `Step 1` and `Step-1` both give `step-1` — and two
 * tabs sharing a slug would make the second unreachable. A name that slugs to nothing at all
 * (`···`) falls back to its index for the same reason.
 */
export function tabSlugs(tabs: TemplateTab[]): string[] {
  const used = new Set<string>();

  return tabs.map((tab, index) => {
    const base = tabSlug(tab.name) || String(index);
    const slug = used.has(base) ? `${base}-${index}` : base;
    used.add(slug);
    return slug;
  });
}

/**
 * Which tab `?tab=` selects. An absent or unrecognised slug lands on the first tab rather than
 * on nothing: the param is user-editable and outlives any template edit that renames a tab.
 */
export function activeTabIndex(tabs: TemplateTab[], tab: string | undefined): number {
  const index = tabSlugs(tabs).indexOf(tab ?? '');
  return index === -1 ? 0 : index;
}
