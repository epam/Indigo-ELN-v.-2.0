import { z } from '@/lib/zod';

import type {
  StoichiometryPart,
  TemplateComponent,
  TemplateComponentType,
  TemplateRequest,
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

export const EMPTY_TEMPLATE_FORM: TemplateFormValues = { name: '', tabs: [EMPTY_TEMPLATE_TAB] };

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
 * Why a tab's name cannot be used, or undefined. Names are unique within a template: the
 * experiment screen opens a tab by its name in the URL, so two equal names would be one tab.
 */
export function tabNameError(tabs: readonly TemplateTabValues[], index: number): string | undefined {
  const name = tabs[index].name.trim();
  if (name === '') return 'Tab Name is required';
  const duplicate = tabs.some((tab, other) => other !== index && tab.name.trim() === name);
  return duplicate ? `Another tab is already called '${name}'` : undefined;
}

/** Why a tab's components cannot be saved, or undefined — the backend wants at least one. */
export function tabComponentsError(tab: TemplateTabValues): string | undefined {
  if (tab.components.length === 0) return 'Pick at least one component';
  if (tab.components.includes('stoichiometryTable') && !Object.values(tab.stoichiometry).some(Boolean)) {
    return 'Pick at least one part of the Stoichiometric Calculation';
  }
  return undefined;
}

/** Whether the whole form would pass — what Save's enablement is computed from. */
export function isTemplateFormValid(values: TemplateFormValues, existing: readonly string[]): boolean {
  return (
    templateNameError(values.name, existing) === undefined &&
    values.tabs.length > 0 &&
    values.tabs.every(
      (tab, index) => tabNameError(values.tabs, index) === undefined && tabComponentsError(tab) === undefined,
    )
  );
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
