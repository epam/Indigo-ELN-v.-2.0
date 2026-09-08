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
