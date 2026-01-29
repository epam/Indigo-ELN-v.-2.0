import { SaltCodeRef } from '@core/types/entities/dictionary.i';
import { EnteredValue } from '@core/types/entities/values.i';
import { MolWeightUnit } from '@core/types/entities/experiments/experiment-shared.i';

export enum CompoundType {
  STORED = 'STORED',
  VIRTUAL = 'VIRTUAL',
  UNKNOWN = 'UNKNOWN',
}

export interface CompoundRef {
  formula?: string;
  casNumber?: string;
  type: CompoundType;
  saltCode?: SaltCodeRef;
  saltEQ?: number;
  compoundKey?: string;
  molWeight?: EnteredValue<MolWeightUnit>;
  exactMass?: number;
  calculatedBatchMF?: string;
}
