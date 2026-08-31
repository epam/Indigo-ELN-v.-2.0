import type { TemplateComponentType, TemplateTab } from '@/lib/types/templates.ts';

/**
 * The collapsible card each component is wrapped in. Hard-coded rather than carried by the
 * template, matching indigo-frontend's `experiment-layout.component.html`, which names the titles
 * in its `@switch` for the same reason: the template data says *what* to show, not what to call it.
 *
 * `null` means the component frames itself and is rendered bare. Three do: `batches` and
 * `versionHistory` bring their own heading, and `stoichiometryTable` draws its own card because
 * the step strip has to sit above it rather than inside it.
 */
export const COMPONENT_TITLES: Record<TemplateComponentType, string | null> = {
  experimentDetails: 'Experiment Details',
  experimentDescription: 'Experiment Description',
  attachments: 'Attachments',
  stoichiometryTable: null,
  batches: null,
  versionHistory: null,
};

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
