import { SaltCodeRef, DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { EnteredValue } from '@core/types/entities/values.i';
import { MolWeightUnit } from '@core/types/entities/experiments/experiment-shared.i';

export enum CompoundType {
  STORED = 'stored',
  VIRTUAL = 'virtual',
  UNKNOWN = 'unknown',
}

export interface CompoundRef {
  compoundID?: string | null;
  molFile: string | null;
  formula: string | null;
  name: string | null;
  casNumber: string | null;
  type: CompoundType;
  stereoisomerCode: DictionaryItemRef | null;
  saltCode: SaltCodeRef | null;
  saltEQ: number | null;
  strCode: string | null;
  molWeight: EnteredValue<MolWeightUnit> | null;
  exactMass: number | null;
  calculatedBatchMF?: string | null;
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
