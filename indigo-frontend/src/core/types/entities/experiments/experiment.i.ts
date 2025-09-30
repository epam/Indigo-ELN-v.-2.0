// Consolidated experiment model types and mutation interfaces
// Auto-generated from OpenAPI schemas for ExperimentModel and nested types
// Source: /mnt/data/swagger spec

import {
  UUID,
  EnteredValueMolUnit,
  EnteredValueWeightUnit,
  EnteredValueVolumeUnit,
  EnteredValueDensityUnit,
  EnteredValueMolarityUnit,
  EnteredValueNoUnit,
  DictionaryItemRef,
  STRCodeCompound,
  STRCodeSample,
  MeltingPoint,
  ExternalSupplier,
  PurityCalculation,
  SolubidityInSolvent,
  ResidualSolvent,
  CompoundRef,
  ReactionInputRole,
  ReactionOutputType,
  SampleRegistrationStatus
} from './experiment-shared.i';
import { Mutation } from './mutation.i';

// ================================
// 1. MUTATION INTERFACES
// ================================
export interface MutateModelForm {
  model?: ExperimentModel;
  mutation?: Mutation;
}

// ================================
// 2. SAMPLE INTERFACES
// ================================
export interface ReactionInputSample {
  anchor: UUID;
  density?: EnteredValueDensityUnit;
  molarity?: EnteredValueMolarityUnit;
  volume?: EnteredValueVolumeUnit;
  purity: EnteredValueNoUnit;
  strCode?: STRCodeSample;
  healthHazard: DictionaryItemRef[];
  row?: ReactionInput;
  sampleId?: UUID;
  mol?: EnteredValueMolUnit;
  weight?: EnteredValueWeightUnit;
}

export interface ReactionOutputSample {
  anchor: UUID;
  density?: EnteredValueDensityUnit;
  molarity?: EnteredValueMolarityUnit;
  volume?: EnteredValueVolumeUnit;
  purity: EnteredValueNoUnit;
  healthHazard: DictionaryItemRef[];
  row?: ReactionOutput;
  actualMol?: EnteredValueMolUnit;
  actualWeight?: EnteredValueWeightUnit;
  yield?: EnteredValueNoUnit;
  registrationStatus?: SampleRegistrationStatus;
  registrationStatusMessage?: string;
  sampleId?: UUID;
  strCode?: STRCodeSample;
  handlingPrecautions: DictionaryItemRef[];
  storageInstructions: DictionaryItemRef[];
  compoundProtection: DictionaryItemRef[];
  solubilityInSolvents: SolubidityInSolvent[];
  residualSolvents: ResidualSolvent[];
  meltingPoint?: MeltingPoint;
  purityCalculations: PurityCalculation[];
  externalSupplier?: ExternalSupplier;
  source?: DictionaryItemRef;
  sourceDetails?: DictionaryItemRef;
  componentState?: DictionaryItemRef;
  batchComment?: string;
  structureComment?: string;
  calculatedMolWeight?: number;
  calculatedBatchMF?: string;
  precursorReactantIds: STRCodeCompound[];
}

// ================================
// 3. REACTION INTERFACES
// ================================
export interface ReactionInput {
  reaction?: Reaction;
  anchor: UUID;
  compound: CompoundRef;
  eq: EnteredValueNoUnit;
  role: ReactionInputRole;
  mol?: EnteredValueMolUnit;
  samples: ReactionInputSample[];
  limiting?: boolean;
}

export interface ReactionOutput {
  reaction?: Reaction;
  anchor: UUID;
  compound: CompoundRef;
  eq: EnteredValueNoUnit;
  type: ReactionOutputType;
  theoMol?: EnteredValueMolUnit;
  theoWeight?: EnteredValueWeightUnit;
  samples: ReactionOutputSample[];
}

export interface Reaction {
  model?: ExperimentModel;
  anchor: UUID;
  rxnfile: string;
  inputs: ReactionInput[];
  outputs: ReactionOutput[];
}

// ================================
// 4. MAIN MODEL INTERFACE
// ================================
export interface ExperimentModel {
  reactions: Reaction[];
}