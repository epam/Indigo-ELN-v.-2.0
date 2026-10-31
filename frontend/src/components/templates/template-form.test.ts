import { describe, expect, it } from 'vitest';

import {
  EMPTY_TEMPLATE_TAB,
  isTemplateFormValid,
  tabComponentsError,
  tabNameError,
  templateNameError,
  toggleComponent,
  toTemplateRequest,
} from '@/components/templates/template-form';

import type { TemplateTabValues } from '@/components/templates/template-form';

function tab(overrides: Partial<TemplateTabValues> = {}): TemplateTabValues {
  return { ...EMPTY_TEMPLATE_TAB, ...overrides };
}

describe('toggleComponent', () => {
  /** The layout order is the list's, never the order the boxes were clicked in. */
  it('keeps the components in layout order whatever order they are ticked in', () => {
    let components = toggleComponent([], 'versionHistory', true);
    components = toggleComponent(components, 'experimentDetails', true);
    components = toggleComponent(components, 'stoichiometryTable', true);
    expect(components).toEqual(['experimentDetails', 'stoichiometryTable', 'versionHistory']);

    expect(toggleComponent(components, 'stoichiometryTable', false)).toEqual(['experimentDetails', 'versionHistory']);
  });
});

describe('templateNameError', () => {
  it('requires a name, and refuses one already taken once trimmed', () => {
    expect(templateNameError('  ', [])).toBe('Template Name is required');
    expect(templateNameError(' Default ', ['Default'])).toBe("Template 'Default' already exists");
    expect(templateNameError('Default 2', ['Default'])).toBeUndefined();
  });
});

describe('tabNameError', () => {
  /** The experiment screen opens a tab by name, so two equal names would be one tab. */
  it('requires a name unique within the template', () => {
    const tabs = [tab({ name: 'Reaction' }), tab({ name: ' Reaction ' }), tab({ name: '' })];
    expect(tabNameError(tabs, 0)).toBe("Another tab is already called 'Reaction'");
    expect(tabNameError(tabs, 2)).toBe('Tab Name is required');
    expect(tabNameError([tab({ name: 'Reaction' })], 0)).toBeUndefined();
  });
});

describe('tabComponentsError', () => {
  it('wants at least one component, and at least one stoichiometry block if that one is on', () => {
    expect(tabComponentsError(tab())).toBe('Pick at least one component');
    expect(
      tabComponentsError(
        tab({
          components: ['stoichiometryTable'],
          stoichiometry: { reactionScheme: false, reactantsReagentsSolvents: false, intendedProducts: false },
        }),
      ),
    ).toBe('Pick at least one part of the Stoichiometric Calculation');
    expect(tabComponentsError(tab({ components: ['attachments'] }))).toBeUndefined();
  });
});

describe('isTemplateFormValid', () => {
  it('passes only when the name and every tab do', () => {
    const good = tab({ name: 'Reaction', components: ['experimentDetails'] });
    expect(isTemplateFormValid({ name: 'New', tabs: [good] }, [])).toBe(true);
    expect(isTemplateFormValid({ name: 'New', tabs: [good, tab({ name: 'Empty' })] }, [])).toBe(false);
    expect(isTemplateFormValid({ name: 'New', tabs: [] }, [])).toBe(false);
    expect(isTemplateFormValid({ name: 'Default', tabs: [good] }, ['Default'])).toBe(false);
  });
});

describe('toTemplateRequest', () => {
  /** Only `stoichiometryTable` carries settings on the wire; the rest are a bare `type`. */
  it('trims names and gives only the stoichiometry component its blocks', () => {
    expect(
      toTemplateRequest({
        name: ' Synthesis ',
        tabs: [
          tab({
            name: ' Reaction ',
            components: ['experimentDetails', 'stoichiometryTable'],
            stoichiometry: { reactionScheme: true, reactantsReagentsSolvents: true, intendedProducts: false },
          }),
        ],
      }),
    ).toEqual({
      name: 'Synthesis',
      templateTabs: [
        {
          name: 'Reaction',
          components: [
            { type: 'experimentDetails' },
            {
              type: 'stoichiometryTable',
              reactionScheme: true,
              reactantsReagentsSolvents: true,
              intendedProducts: false,
            },
          ],
        },
      ],
    });
  });
});
