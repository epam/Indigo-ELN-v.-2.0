// Experiment model types and nested structures
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
  ReactionOutputType,
  SampleRegistrationStatus,
  ReactionRole,
} from './experiment-shared.i';
import { CompoundRef } from '@core/types/entities/compound.i';

// ================================
// 1. SAMPLE INTERFACES
// ================================
export interface ReactionInputSample {
  anchor: UUID;
  chemicalName?: string;
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
  comment?: string;
}

export interface ReactionOutputSample {
  anchor: UUID;
  nbkBatchNumber: string;
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
// 2. REACTION INTERFACES
// ================================
export interface ReactionInput {
  reaction?: Reaction;
  anchor: UUID;
  compound: CompoundRef;
  eq: EnteredValueNoUnit;
  role: ReactionRole;
  mol?: EnteredValueMolUnit;
  samples: ReactionInputSample[];
  limiting?: boolean;
}

export interface ReactionOutput {
  reaction?: Reaction;
  anchor: UUID;
  chemicalName?: string;
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
  rxnVersion: number;
  inputs: ReactionInput[];
  outputs: ReactionOutput[];
}

// ================================
// 3. MAIN MODEL INTERFACE
// ================================
export interface ExperimentModel {
  reactions: Reaction[];
}
