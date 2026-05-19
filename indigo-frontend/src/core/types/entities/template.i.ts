import { BaseEntity } from './base-entity.i';

export interface TemplateComponentExperimentDetails {
  type: 'experimentDetails';
}

export interface TemplateComponentExperimentDescription {
  type: 'experimentDescription';
}

export interface TemplateComponentAttachments {
  type: 'attachments';
}

export interface TemplateComponentStoichiometryTable {
  type: 'stoichiometryTable';
  reactionScheme: boolean;
  reactantsReagentsSolvents: boolean;
  intendedProducts: boolean;
}

export interface TemplateComponentBatches {
  type: 'batches';
}

export interface TemplateComponentVersionHistory {
  type: 'versionHistory';
}

export type TemplateComponent =
  | TemplateComponentExperimentDetails
  | TemplateComponentExperimentDescription
  | TemplateComponentAttachments
  | TemplateComponentStoichiometryTable
  | TemplateComponentBatches
  | TemplateComponentVersionHistory;

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
