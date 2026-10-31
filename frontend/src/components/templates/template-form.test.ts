import { describe, expect, it } from 'vitest';

import {
  EMPTY_TEMPLATE_TAB,
  startingTemplateForm,
  tabComponentsError,
  tabNameError,
  templateFormErrors,
  templateNameError,
  toggleComponent,
  toTemplateRequest,
} from '@/components/templates/template-form';
import { makeTemplateDetails } from '@/mocks/fixtures';

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
  /** Only required: the backend allows two tabs one name, and the experiment screen copes. */
  it('requires a name, and nothing more', () => {
    expect(tabNameError(tab({ name: '  ' }))).toBe('Tab Name is required');
    expect(tabNameError(tab({ name: 'Reaction' }))).toBeUndefined();
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

describe('templateFormErrors', () => {
  /** Keyed the way a TanStack form-level validator hands errors to its fields. */
  it('puts each message under the field it belongs to, and is empty when all is well', () => {
    const good = tab({ name: 'Reaction', components: ['experimentDetails'] });
    expect(templateFormErrors({ name: 'New', tabs: [good] }, [])).toEqual({});
    expect(templateFormErrors({ name: 'Default', tabs: [good, tab({ name: '' })] }, ['Default'])).toEqual({
      name: "Template 'Default' already exists",
      'tabs[1].name': 'Tab Name is required',
      'tabs[1].components': 'Pick at least one component',
    });
  });
});

describe('startingTemplateForm', () => {
  /** A copy of the starter's tabs, so a name and Save are enough — issue #739's steps. */
  it("copies the starter's tabs, its stoichiometry blocks included, with no name", () => {
    const starter = makeTemplateDetails().templateTabs;
    const form = startingTemplateForm(starter);
    expect(form.name).toBe('');
    expect(form.tabs.map((copy) => copy.name)).toEqual(starter.map((original) => original.name));
    expect(toTemplateRequest({ ...form, name: 'Copy' }).templateTabs).toEqual(starter);
    expect(templateFormErrors({ ...form, name: 'Copy' }, ['Default'])).toEqual({});
  });

  it('falls back to one "Tab 1" with nothing ticked, as the Angular app began', () => {
    expect(startingTemplateForm(undefined).tabs).toEqual([{ ...EMPTY_TEMPLATE_TAB, name: 'Tab 1' }]);
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
