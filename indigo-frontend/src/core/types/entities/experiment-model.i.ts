import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { CompoundRef } from '@core/types/entities/compound.i';
import { EnteredValue } from '@core/types/entities/values.i';

export enum ReactionInputRole {
  REACTANT = 'REACTANT',
  REAGENT = 'REAGENT',
  SOLVENT = 'SOLVENT',
}

export interface ReactionRow {
  anchor: string;
  compound: CompoundRef;
  eq: EnteredValue /*NoUnit*/;
}

export interface ReactionSample {
  anchor: string;
  density: EnteredValue /*DensityUnit*/ | null;
  molarity: EnteredValue /*MolarityUnit*/ | null;
  volume: EnteredValue /*VolumeUnit*/ | null;
  purity: EnteredValue /*NoUnit*/;
  strCode: string | null;
  healthHazard: DictionaryItemRef[];
}

export interface ReactionInputSample extends ReactionSample {
  sampleId: string | null;
  mol: EnteredValue /*MolUnit*/ | null;
  weight: EnteredValue /*WeightUnit*/ | null;
}

export interface ReactionInput extends ReactionRow {
  role: ReactionInputRole;
  mol: EnteredValue /*MolUnit*/ | null;
  samples: ReactionInputSample[];
  limiting: boolean;
}

export enum ReactionOutputType {
  FINAL = 'FINAL',
  BY_PRODUCT = 'BY_PRODUCT',
  INTERMEDIATE = 'INTERMEDIATE',
}

export enum SampleRegistrationStatus {
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

export enum SolubidityType {
  QUANTITATIVE = 'QUANTITATIVE',
  QUALITATIVE = 'QUALITATIVE',
}

export enum ComparisonOperator {
  GREATER_THAN = 'GREATER_THAN',
  LESS_THAN = 'LESS_THAN',
  EQUALS = 'EQUALS',
  APPROXIMATELY = 'APPROXIMATELY',
}

export enum SolubidityQualitativeType {
  SOLUBLE = 'SOLUBLE',
  UNSOLUBLE = 'UNSOLUBLE',
  PRECIPITATE = 'PRECIPITATE',
}

export interface SolubidityInSolvent {
  solvent: DictionaryItemRef;
  comment: string;
  type: SolubidityType;
  operator: ComparisonOperator;
  value: number | null;
  unit: string /*DensityUnit*/ | null;
  qualitativeType: SolubidityQualitativeType | null;
}

export interface ResidualSolvent {
  solvent: DictionaryItemRef;
  eq: number;
  comment: string | null;
}

export interface MeltingPoint {
  lower: number | null;
  upper: number | null;
  comment: string | null;
}

export enum PurityCalculationType {
  NMR = 'NMR',
  HPLC = 'HPLC',
  LCMS = 'LCMS',
  CHN = 'CHN',
  MS = 'MS',
}

export interface PurityCalculation {
  type: PurityCalculationType;
  operator: ComparisonOperator;
  purity: number;
  comment: string | null;
}

export interface ExternalSupplier {
  supplier: DictionaryItemRef;
  registryNumber: string;
}

export interface ReactionOutputSample extends ReactionSample {
  actualMol: EnteredValue /*MolUnit*/ | null;
  actualWeight: EnteredValue /*WeightUnit*/ | null;
  yield: EnteredValue /*NoUnit*/ | null;
  registrationStatus: SampleRegistrationStatus | null;
  registrationStatusMessage: string | null;
  sampleId: string | null;
  strCode: string | null;
  handlingPrecautions: DictionaryItemRef[];
  storageInstructions: DictionaryItemRef[];
  compoundProtection: DictionaryItemRef[];
  solubilityInSolvents: SolubidityInSolvent[];
  residualSolvents: ResidualSolvent[];
  meltingPoint: MeltingPoint | null;
  purityCalculations: PurityCalculation[];
  externalSupplier: ExternalSupplier | null;
  source: DictionaryItemRef | null;
  sourceDetails: DictionaryItemRef | null;
  componentState: DictionaryItemRef | null;
  batchComment: string | null;
  structureComment: string | null;
}

export interface ReactionOutput {
  type: ReactionOutputType;
  theoMol: EnteredValue /*MolUnit*/ | null;
  theoWeight: EnteredValue /*WeightUnit*/ | null;
  samples: ReactionOutputSample[];
}

export interface Reaction {
  anchor: string;
  rxnfile: string;
  inputs: ReactionInput[];
  outputs: ReactionOutput[];
}

export interface ExperimentModel {
  reactions: Reaction[];
}
