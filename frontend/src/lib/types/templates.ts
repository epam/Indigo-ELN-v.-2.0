import type { BaseDTO } from '@/lib/types/common.ts';

/**
 * Mirrors TemplateComponent (eln-api, eln/model) — a sealed interface Jackson serialises with
 * `@JsonTypeInfo(Id.NAME, property = "type")`, so the wire shape is a discriminated union on
 * `type`. These six are the whole set the backend permits; adding a seventh here is what makes
 * `ExperimentTemplateTab`'s switch fail to compile until it is handled.
 *
 * Only `stoichiometryTable` carries configuration: the three booleans say which of its sub-blocks
 * the template asked for.
 */
export type TemplateComponent =
  | { type: 'experimentDetails' }
  | { type: 'experimentDescription' }
  | { type: 'attachments' }
  | {
      type: 'stoichiometryTable';
      reactionScheme: boolean;
      reactantsReagentsSolvents: boolean;
      intendedProducts: boolean;
    }
  | { type: 'batches' }
  | { type: 'versionHistory' };

export type TemplateComponentType = TemplateComponent['type'];

/**
 * The collapsible card each component is wrapped in. Hard-coded rather than carried by the
 * template: the template data says *what* to show, not what to call it.
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

/** Mirrors TemplateTab. The name is what the experiment screen's tab strip shows. */
export interface TemplateTab {
  name: string;
  components: TemplateComponent[];
}

/** Mirrors TemplateDTO — the shape the paged /templates list returns. */
export interface Template extends BaseDTO {
  name: string;
}

/**
 * TemplateDetailsDTO — the single-template response, and the thing that dictates an experiment's
 * whole layout: which tabs it has, and which components sit in each.
 */
export interface TemplateDetails extends Template {
  templateTabs: TemplateTab[];
}

/** TemplateEntity.name is `@Size(max = 256)`. */
export const TEMPLATE_NAME_MAX_LENGTH = 256;

/**
 * Every component type, in the order a tab lays them out when a template is built in the app —
 * the order the experiment screen reads top to bottom. `satisfies` against the union keeps a new
 * backend type from being left off.
 */
export const TEMPLATE_COMPONENT_TYPES = [
  'experimentDetails',
  'experimentDescription',
  'stoichiometryTable',
  'batches',
  'attachments',
  'versionHistory',
] as const satisfies readonly TemplateComponentType[];

/**
 * What each component is called when a template is described rather than rendered — the template
 * editor and its read-only view. Unlike `COMPONENT_TITLES` every type has one, named after the
 * heading the experiment screen gives it.
 */
export const COMPONENT_LABELS: Record<TemplateComponentType, string> = {
  experimentDetails: 'Experiment Details',
  experimentDescription: 'Experiment Description',
  attachments: 'Attachments',
  stoichiometryTable: 'Stoichiometric Calculation',
  batches: 'Product Batch Summary',
  versionHistory: 'Version History',
};

/** The three sub-blocks a `stoichiometryTable` component switches on, keyed as on the wire. */
export type StoichiometryPart = 'reactionScheme' | 'reactantsReagentsSolvents' | 'intendedProducts';

/** In the order `StoichiometryPanel` stacks them, under the headings it gives them. */
export const STOICHIOMETRY_PART_LABELS: Record<StoichiometryPart, string> = {
  reactionScheme: 'Reaction Scheme',
  reactantsReagentsSolvents: 'Reactants, Reagents, Solvents',
  intendedProducts: 'Reaction Products',
};

/** Body of `POST /templates`. Both fields are `@NotEmpty`, and so is every tab's `components`. */
export interface TemplateRequest {
  name: string;
  templateTabs: TemplateTab[];
}
