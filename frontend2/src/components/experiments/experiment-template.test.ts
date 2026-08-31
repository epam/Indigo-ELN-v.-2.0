import { describe, expect, it } from 'vitest';

import { activeTabIndex, tabSlug, tabSlugs } from '@/components/experiments/experiment-template';
import type { TemplateTab } from '@/lib/types/templates.ts';

function tabs(...names: string[]): TemplateTab[] {
  return names.map((name) => ({ name, components: [] }));
}

describe('tabSlug', () => {
  it('lowercases and joins words with a dash', () => {
    expect(tabSlug('Previous Versions')).toBe('previous-versions');
  });

  it('collapses a run of punctuation into one dash and trims the ends', () => {
    expect(tabSlug('  Reaction / Scheme!  ')).toBe('reaction-scheme');
  });

  it('keeps digits, which tab names use for steps', () => {
    expect(tabSlug('Step 2')).toBe('step-2');
  });
});

describe('tabSlugs', () => {
  it('slugs every tab in order', () => {
    expect(tabSlugs(tabs('Experiment Info', 'Attachments'))).toEqual(['experiment-info', 'attachments']);
  });

  /** Two tabs sharing a slug would make the second unreachable from the URL. */
  it('disambiguates names that slug alike by index', () => {
    expect(tabSlugs(tabs('Step 1', 'Step-1'))).toEqual(['step-1', 'step-1-1']);
  });

  it('falls back to the index for a name with nothing sluggable in it', () => {
    expect(tabSlugs(tabs('Info', '···'))).toEqual(['info', '1']);
  });
});

describe('activeTabIndex', () => {
  const four = tabs('Experiment Info', 'Attachments', 'Summary', 'Previous Versions');

  it('finds the tab the slug names', () => {
    expect(activeTabIndex(four, 'summary')).toBe(2);
  });

  it('falls back to the first tab when the param is absent', () => {
    expect(activeTabIndex(four, undefined)).toBe(0);
  });

  /** The param is user-editable and outlives a template edit that renames a tab. */
  it('falls back to the first tab when the slug matches nothing', () => {
    expect(activeTabIndex(four, 'nope')).toBe(0);
  });
});
