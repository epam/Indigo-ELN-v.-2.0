// Form interface for mutating experiment models
// Auto-generated from OpenAPI schemas
// Source: /mnt/data/swagger spec

import { ExperimentModel } from './experiment.i';
import { Mutation } from './mutation.i';

export interface MutateModelForm {
  model?: ExperimentModel;
  mutation?: Mutation;
}
