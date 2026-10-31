import { z } from '@/lib/zod';

import type {
  StoichiometryPart,
  TemplateComponent,
  TemplateComponentType,
  TemplateRequest,
  TemplateTab,
} from '@/lib/types/templates.ts';
import { TEMPLATE_COMPONENT_TYPES, TEMPLATE_NAME_MAX_LENGTH } from '@/lib/types/templates.ts';

export const templateNameSchema = z
  .string()
  .trim()
  .min(1, 'Template Name is required')
  .max(TEMPLATE_NAME_MAX_LENGTH, `Use ${TEMPLATE_NAME_MAX_LENGTH} characters at most`);

/**
 * One tab as the editor holds it. Components are a list of types rather than wire objects: a
 * checkbox per type is the whole UI, and only `stoichiometryTable` has settings, kept beside them
 * so they survive the component being unticked and ticked again.
 */
export interface TemplateTabValues {
  name: string;
  components: TemplateComponentType[];
  stoichiometry: Record<StoichiometryPart, boolean>;
}

export interface TemplateFormValues {
  name: string;
  tabs: TemplateTabValues[];
}

/** A new tab: nothing ticked, and every stoichiometry block on for when that component is. */
export const EMPTY_TEMPLATE_TAB: TemplateTabValues = {
  name: '',
  components: [],
  stoichiometry: { reactionScheme: true, reactantsReagentsSolvents: true, intendedProducts: true },
};

/**
 * The template a new one starts as a copy of. The backend seeds it and looks it up by this name
 * itself (`SupportService`), so the name is as stable as anything it relies on.
 */
export const STARTER_TEMPLATE_NAME = 'Default';

/**
 * Add Template's starting point: an unnamed copy of `starter`'s tabs, so that, as issue #739's
 * steps have it, a name and Save are enough. With no starter, one tab called "Tab 1", as the
 * Angular app began — which still needs a component before it can be saved.
 */
export function startingTemplateForm(starter: readonly TemplateTab[] | undefined): TemplateFormValues {
  if (!starter?.length) return { name: '', tabs: [{ ...EMPTY_TEMPLATE_TAB, name: 'Tab 1' }] };
  return {
    name: '',
    tabs: starter.map((tab) => {
      const stoichiometry = tab.components.find((component) => component.type === 'stoichiometryTable');
      return {
        name: tab.name,
        components: tab.components.map((component) => component.type),
        stoichiometry: stoichiometry
          ? {
              reactionScheme: stoichiometry.reactionScheme,
              reactantsReagentsSolvents: stoichiometry.reactantsReagentsSolvents,
              intendedProducts: stoichiometry.intendedProducts,
            }
          : EMPTY_TEMPLATE_TAB.stoichiometry,
      };
    }),
  };
}

/**
 * Ticks or unticks a component, keeping the tab in `TEMPLATE_COMPONENT_TYPES` order — so the order
 * a template lays out its components in never depends on the order they were clicked.
 */
export function toggleComponent(
  components: readonly TemplateComponentType[],
  type: TemplateComponentType,
  on: boolean,
): TemplateComponentType[] {
  const next = new Set(components);
  if (on) next.add(type);
  else next.delete(type);
  return TEMPLATE_COMPONENT_TYPES.filter((candidate) => next.has(candidate));
}

/** Why a template name cannot be used, or undefined. `existing` is the list the page already has. */
export function templateNameError(name: string, existing: readonly string[]): string | undefined {
  const parsed = templateNameSchema.safeParse(name);
  if (!parsed.success) return parsed.error.issues[0]?.message;
  // The backend's unique constraint answers too, but only once Save has been pressed.
  return existing.includes(parsed.data) ? `Template '${parsed.data}' already exists` : undefined;
}

/**
 * Why a tab's name cannot be used, or undefined. Only that it has one: two tabs may share a name,
 * as the backend allows — the experiment screen's `tabSlugs` already tells them apart.
 */
export function tabNameError(tab: TemplateTabValues): string | undefined {
  return tab.name.trim() === '' ? 'Tab Name is required' : undefined;
}

/** Why a tab's components cannot be saved, or undefined — the backend wants at least one. */
export function tabComponentsError(tab: TemplateTabValues): string | undefined {
  if (tab.components.length === 0) return 'Pick at least one component';
  if (tab.components.includes('stoichiometryTable') && !Object.values(tab.stoichiometry).some(Boolean)) {
    return 'Pick at least one part of the Stoichiometric Calculation';
  }
  return undefined;
}

/**
 * Every problem with the form, keyed by the field it belongs to — the shape a TanStack form-level
 * validator returns as `fields`, so each message lands on its own field. Empty when it would pass.
 * One function for the whole form because the template name is checked against the list the page
 * holds, which no single field knows about.
 */
export function templateFormErrors(values: TemplateFormValues, existing: readonly string[]): Record<string, string> {
  const errors: Record<string, string> = {};
  const nameError = templateNameError(values.name, existing);
  if (nameError) errors.name = nameError;
  values.tabs.forEach((tab, index) => {
    const tabName = tabNameError(tab);
    if (tabName) errors[`tabs[${index}].name`] = tabName;
    const components = tabComponentsError(tab);
    if (components) errors[`tabs[${index}].components`] = components;
  });
  return errors;
}

function toComponent(type: TemplateComponentType, tab: TemplateTabValues): TemplateComponent {
  return type === 'stoichiometryTable' ? { type, ...tab.stoichiometry } : { type };
}

/** The `POST /templates` body, names trimmed. */
export function toTemplateRequest(values: TemplateFormValues): TemplateRequest {
  return {
    name: values.name.trim(),
    templateTabs: values.tabs.map((tab) => ({
      name: tab.name.trim(),
      components: tab.components.map((type) => toComponent(type, tab)),
    })),
  };
}
