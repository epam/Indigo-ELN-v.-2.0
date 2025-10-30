import { SaltCodeRef } from '@core/types/entities/dictionary.i';
import { EnteredValue } from '@core/types/entities/values.i';
import { MolWeightUnit } from '@core/types/entities/experiments/experiment-shared.i';

export interface CompoundRef {
  molFile: string | null;
  formula: string | null;
  name: string | null;
  saltCode: SaltCodeRef | null;
  saltEQ: number | null;
  strCode: string | null;
  molWeight: EnteredValue<MolWeightUnit> | null;
  exactMass: number | null;
}

export interface StoredCompoundRef extends CompoundRef {
  type: 'stored';
  compoundID: string;
  name: string | null;
  molWeight: EnteredValue<MolWeightUnit>;
  molFile: string;
  formula: string;
}

export interface VirtualCompoundRef extends CompoundRef {
  type: 'virtual';
  compoundID: string;
  molFile: string;
  formula: string;
  molWeight: EnteredValue<MolWeightUnit>;
}

export interface UnknownCompoundRef extends CompoundRef {
  type: 'unknown';
}
