import { SaltCodeRef, DictionaryItemRef } from '@core/types/entities/dictionary.i';
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
  // Additional fields from ng-411
  compoundID?: string | null;
  molFile?: string | null;
  name?: string | null;
  stereoisomerCode?: DictionaryItemRef | null;
  strCode?: string | null;
}

export interface StoredCompoundRef extends CompoundRef {
  type: CompoundType.STORED;
  compoundID: string;
  name: string | null;
  molWeight: EnteredValue<MolWeightUnit>;
  molFile: string;
  formula: string;
}

export interface VirtualCompoundRef extends CompoundRef {
  type: CompoundType.VIRTUAL;
  compoundID: string;
  molFile: string;
  formula: string;
  molWeight: EnteredValue<MolWeightUnit>;
}

export interface UnknownCompoundRef extends CompoundRef {
  type: CompoundType.UNKNOWN;
}
