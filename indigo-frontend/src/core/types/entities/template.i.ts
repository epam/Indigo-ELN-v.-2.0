import { BaseEntity } from './base-entity.i';

export interface TemplateComponentAttachments {
  type: 'attachments';
}

export interface TemplateComponentBatches {
  type: 'batches';
}

export interface TemplateComponentConceptDetails {
  type: 'conceptDetails';
}

export interface TemplateComponentExperimentDetails {
  type: 'experimentDetails';
}

export interface TemplateComponentExperimentDescription {
  type: 'experimentDescription';
}

export interface TemplateComponentPreferredCompoundsDetails {
  type: 'preferredCompoundsDetails';
}

export interface TemplateComponentPreferredCompoundsSummary {
  type: 'preferredCompoundsSummary';
}

export interface TemplateComponentReactionDetails {
  type: 'reactionsDetails';
}

export interface TemplateComponentStoichiometryTable {
  type: 'stoichiometryTable';
  reactantsReagentsSolvents: boolean;
  reactionProducts: boolean;
}

export interface TemplateComponentReactionScheme {
  type: 'reactionScheme';
}

export interface TemplateComponentReactants {
  type: 'reactants';
}

export interface TemplateComponentIntendedProducts {
  type: 'intendedProducts';
}

export type TemplateComponent =
  | TemplateComponentAttachments
  | TemplateComponentBatches
  | TemplateComponentConceptDetails
  | TemplateComponentExperimentDetails
  | TemplateComponentExperimentDescription
  | TemplateComponentPreferredCompoundsDetails
  | TemplateComponentPreferredCompoundsSummary
  | TemplateComponentReactionDetails
  | TemplateComponentStoichiometryTable
  | TemplateComponentReactionScheme
  | TemplateComponentReactants
  | TemplateComponentIntendedProducts;

export interface TemplateTab {
  name: string;
  components: TemplateComponent[];
}

export interface Template extends BaseEntity {
  name: string;
  templateTabs: TemplateTab[];
}

export interface RootTemplate {
  pageNo: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  items: ItemTemplate[];
}

export interface ItemTemplate {
  id: string;
  createdBy: CreatedBy;
  createdAt: string;
  modifiedBy: CreatedBy;
  modifiedAt: string;
  name: string;
}

export interface CreatedBy {
  id: string;
  username: string;
  displayName: string;
}
