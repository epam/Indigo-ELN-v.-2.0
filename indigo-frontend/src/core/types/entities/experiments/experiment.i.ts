// Experiment model types and nested structures
// Auto-generated from OpenAPI schemas for ExperimentModel and nested types
// Source: /mnt/data/swagger spec

import {
  DensityUnit,
  ExternalSupplier,
  MeltingPoint,
  MolarityUnit,
  MolUnit,
  NoUnit,
  PurityCalculation,
  ReactionOutputType,
  ReactionRole,
  ResidualSolvent,
  SampleRegistrationStatus,
  SolubidityInSolvent,
  STRCodeCompound,
  STRCodeSample,
  UUID,
  VolumeUnit,
  WeightUnit,
} from './experiment-shared.i';
import { CompoundRef } from '@core/types/entities/compound.i';
import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { EnteredValue } from '@core/types/entities/values.i';

// ================================
// 1. SAMPLE INTERFACES
// ================================
export interface ReactionInputSample {
  anchor: UUID;
  nbkBatchNumber?: string;
  density?: EnteredValue<DensityUnit>;
  molarity?: EnteredValue<MolarityUnit>;
  volume?: EnteredValue<VolumeUnit>;
  purity: EnteredValue<NoUnit>;
  strCode?: STRCodeSample;
  healthHazards: DictionaryItemRef[];
  sampleId?: UUID;
  mol?: EnteredValue<MolUnit>;
  weight?: EnteredValue<WeightUnit>;
  comment?: string;
}

export interface ReactionOutputSample {
  anchor: UUID;
  nbkBatchNumber: string;
  shortNbkBatchNumber: string;
  density?: EnteredValue<DensityUnit>;
  molarity?: EnteredValue<MolarityUnit>;
  volume?: EnteredValue<VolumeUnit>;
  purity: EnteredValue<NoUnit>;
  healthHazards: DictionaryItemRef[];
  actualMol?: EnteredValue<MolUnit>;
  actualWeight?: EnteredValue<WeightUnit>;
  yield?: EnteredValue<NoUnit>;
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
}

// ================================
// 2. REACTION INTERFACES
// ================================
export interface ReactionInput {
  anchor: UUID;
  compound: CompoundRef;
  eq: EnteredValue<NoUnit>;
  role: ReactionRole;
  mol?: EnteredValue<MolUnit>;
  chemicalName?: string;
  samples: ReactionInputSample[];
  limiting: boolean;
  rxnPosition?: number;
}

export interface ReactionOutput {
  anchor: UUID;
  outputName: string;
  chemicalName?: string;
  compound: CompoundRef;
  eq: EnteredValue<NoUnit>;
  type: ReactionOutputType;
  theoMol?: EnteredValue<MolUnit>;
  theoWeight?: EnteredValue<WeightUnit>;
  samples: ReactionOutputSample[];
}

export interface Reaction {
  anchor: UUID;
  rxnfile: string;
  rxnVersion: number;
  inputs: ReactionInput[];
  outputs: ReactionOutput[];
  precursorReactantIds: STRCodeCompound[];
}

// ================================
// 3. MAIN MODEL INTERFACE
// ================================
export interface ExperimentModel {
  reactions: Reaction[];
  significantFigures: number;
}
