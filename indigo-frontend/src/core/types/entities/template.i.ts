import { BaseEntity } from './base-entity.i';

export interface TemplateTab {
  name: string;
  link: string;
}

// TODO actualize the list when it is updated on the backend
type TemplateComponent =
  | {
      type: 'reactionScheme';
    }
  | {
      type: 'stoichiometryTable';
      reactantsReagentsSolvents: boolean;
      reactionProducts: boolean;
    }
  | {
      type: 'batches';
    }
  | {
      type: 'attachments';
    }
  | {
      type: 'experimentDescription';
    }
  | {
      type: 'experimentDetails';
    }
  | {
      type: 'conceptDetails';
    };

export interface Template extends BaseEntity {
  name: string;
  components: TemplateComponent[];
}
