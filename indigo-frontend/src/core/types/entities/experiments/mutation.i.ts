import {
  DensityUnit,
  ExternalSupplier,
  MeltingPoint,
  MolarityUnit,
  MolUnit,
  PurityCalculation,
  ReactionOutputType,
  ReactionRole,
  ResidualSolvent,
  SolubidityInSolvent,
  UUID,
  VolumeUnit,
  WeightUnit,
} from './experiment-shared.i';
import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { UserMetadata } from '@core/types/entities/user.i';

// Base mutation interface
// eslint-disable-next-line @typescript-eslint/no-empty-object-type
interface BaseMutation {}

export type ReactionAnchor = string;
export type ReactionInputAnchor = string;
export type ReactionInputSampleAnchor = string;
export type ReactionOutputAnchor = string;
export type ReactionOutputSampleAnchor = string;

interface SetBatchCreatorMutation extends BaseMutation {
  batchCreator: UserMetadata;
}

interface ReactionMutation extends BaseMutation {
  anchor: ReactionAnchor;
}

interface SetSchemeMutation extends ReactionMutation {
  type: 'SetScheme';
  rxnFile: string;
}

interface ResolveInputsMutation extends ReactionMutation {
  type: 'ResolveInputs';
  inputSamples: Record<ReactionInputAnchor, UUID>;
}

interface AddEmptyInputMutation extends ReactionMutation {
  type: 'AddEmptyInput';
}

interface AddInput extends ReactionMutation {
  type: 'AddInput';
  sampleId: UUID;
}

interface RemoveInputMutation extends ReactionMutation {
  type: 'RemoveInput';
  input: ReactionInputAnchor;
}

interface ReactionInputMutation extends BaseMutation {
  anchor: ReactionInputAnchor;
}

interface SetInputRowRole extends ReactionInputMutation {
  type: 'SetInputRowRole';
  role: ReactionRole;
}

interface SetInputRowMol extends ReactionInputMutation {
  type: 'SetInputRowMol';
  mol: number | null;
  molUnit: MolUnit | null;
}

interface SetInputRowLimiting extends ReactionInputMutation {
  type: 'SetInputRowLimiting';
}

interface SetInputRowSaltCode extends ReactionInputMutation {
  type: 'SetInputRowSaltCode';
  saltCode: DictionaryItemRef | null;
}

interface SetInputRowSaltEQ extends ReactionInputMutation {
  type: 'SetInputRowSaltEQ';
  saltEQ: number | null;
}

interface SetInputRowEQ extends ReactionInputMutation {
  type: 'SetInputRowEQ';
  eq: number | null;
}

interface SetInputCompoundStereoisomerCode extends ReactionInputMutation {
  type: 'SetInputCompoundStereoisomerCode';
  stereoisomerCode: DictionaryItemRef | null;
}

interface SetInputCompoundMolWeight extends ReactionInputMutation {
  type: 'SetInputCompoundMolWeight';
  molWeight: number | null;
}

interface ReactionInputSampleMutation extends BaseMutation {
  anchor: ReactionInputSampleAnchor;
}

interface SetInputDensity extends ReactionInputSampleMutation {
  type: 'SetInputDensity';
  density: number | null;
  unit: DensityUnit | null;
}

interface SetInputMolarity extends ReactionInputSampleMutation {
  type: 'SetInputMolarity';
  molarity: number | null;
  unit: MolarityUnit | null;
}

interface SetInputVolume extends ReactionInputSampleMutation {
  type: 'SetInputVolume';
  volume: number | null;
  unit: VolumeUnit | null;
}

interface SetInputPurity extends ReactionInputSampleMutation {
  type: 'SetInputPurity';
  purity: number | null;
}

interface SetInputHealthHazards extends ReactionInputSampleMutation {
  type: 'SetInputHealthHazards';
  healthHazards: DictionaryItemRef[];
}

interface SetInputMol extends ReactionInputSampleMutation {
  type: 'SetInputMol';
  mol: number | null;
  unit: MolUnit | null;
}

interface SetInputWeight extends ReactionInputSampleMutation {
  type: 'SetInputWeight';
  weight: number | null;
  unit: WeightUnit | null;
}

interface SetInputComment extends ReactionInputSampleMutation {
  type: 'SetInputComment';
  comment: string | null;
}

interface ReactionOutputMutation extends BaseMutation {
  anchor: ReactionOutputAnchor;
}

interface AddProductSample extends ReactionOutputMutation {
  type: 'AddProductSample';
}

interface SetOutputRowType extends ReactionOutputMutation {
  type: 'SetOutputRowType';
  outputType: ReactionOutputType;
}

interface SetOutputRowSaltCode extends ReactionOutputMutation {
  type: 'SetOutputRowSaltCode';
  saltCode: DictionaryItemRef | null;
}

interface SetOutputRowSaltEQ extends ReactionOutputMutation {
  type: 'SetOutputRowSaltEQ';
  saltEQ: number | null;
}

interface SetOutputRowEQ extends ReactionOutputMutation {
  type: 'SetOutputRowEQ';
  eq: number | null;
}

interface SetOutputRowName extends ReactionOutputMutation {
  type: 'SetOutputRowName';
  name: string;
}

interface SetOutputRowChemicalName extends ReactionOutputMutation {
  type: 'SetOutputRowChemicalName';
  chemicalName: string | null;
}

interface SetOutputCompoundStereoisomerCode extends ReactionOutputMutation {
  type: 'SetOutputCompoundStereoisomerCode';
  stereoisomerCode: DictionaryItemRef | null;
}

interface SetOutputCompoundMolWeight extends ReactionOutputMutation {
  type: 'SetOutputCompoundMolWeight';
  molWeight: number | null;
}

interface ReactionOutputSampleMutation extends BaseMutation {
  anchor: ReactionOutputSampleAnchor;
}

interface SetOutputDensity extends ReactionOutputSampleMutation {
  type: 'SetOutputDensity';
  density: number | null;
  unit: DensityUnit | null;
}

interface SetOutputMolarity extends ReactionOutputSampleMutation {
  type: 'SetOutputMolarity';
  molarity: number | null;
  unit: MolarityUnit | null;
}

interface SetOutputVolume extends ReactionOutputSampleMutation {
  type: 'SetOutputVolume';
  volume: number | null;
  unit: VolumeUnit | null;
}

interface SetOutputPurity extends ReactionOutputSampleMutation {
  type: 'SetOutputPurity';
  purity: number | null;
}

interface SetOutputHealthHazards extends ReactionOutputSampleMutation {
  type: 'SetOutputHealthHazards';
  healthHazards: DictionaryItemRef[];
}

interface SetOutputActualMol extends ReactionOutputSampleMutation {
  type: 'SetOutputActualMol';
  actualMol: number | null;
  unit: MolUnit | null;
}

interface SetOutputActualWeight extends ReactionOutputSampleMutation {
  type: 'SetOutputActualWeight';
  actualWeight: number | null;
  unit: WeightUnit | null;
}

interface RegisterSample extends ReactionOutputSampleMutation {
  type: 'RegisterSample';
}

interface SetOutputHandlingPrecautions extends ReactionOutputSampleMutation {
  type: 'SetOutputHandlingPrecautions';
  handlingPrecautions: DictionaryItemRef[];
}

interface SetOutputStorageInstructions extends ReactionOutputSampleMutation {
  type: 'SetOutputStorageInstructions';
  storageInstructions: DictionaryItemRef[];
}

interface SetOutputCompoundProtection extends ReactionOutputSampleMutation {
  type: 'SetOutputCompoundProtection';
  compoundProtection: DictionaryItemRef[];
}

interface SetOutputSolubilityInSolvents extends ReactionOutputSampleMutation {
  type: 'SetOutputSolubilityInSolvents';
  solubilityInSolvents: SolubidityInSolvent[];
}

interface SetOutputResidualSolvents extends ReactionOutputSampleMutation {
  type: 'SetOutputResidualSolvents';
  residualSolvents: ResidualSolvent[];
}

interface SetOutputMeltingPoint extends ReactionOutputSampleMutation {
  type: 'SetOutputMeltingPoint';
  meltingPoint: MeltingPoint | null;
}

interface SetOutputPurityCalculations extends ReactionOutputSampleMutation {
  type: 'SetOutputPurityCalculations';
  purityCalculations: PurityCalculation[];
}

interface SetOutputExternalSupplier extends ReactionOutputSampleMutation {
  type: 'SetOutputExternalSupplier';
  externalSupplier: ExternalSupplier;
}

interface SetOutputSource extends ReactionOutputSampleMutation {
  type: 'SetOutputSource';
  source: DictionaryItemRef | null;
}

interface SetOutputSourceDetails extends ReactionOutputSampleMutation {
  type: 'SetOutputSourceDetails';
  sourceDetails: DictionaryItemRef | null;
}

interface SetOutputComponentState extends ReactionOutputSampleMutation {
  type: 'SetOutputComponentState';
  componentState: DictionaryItemRef | null;
}

interface SetOutputBatchComment extends ReactionOutputSampleMutation {
  type: 'SetOutputBatchComment';
  batchComment: string | null;
}

interface SetOutputStructureComment extends ReactionOutputSampleMutation {
  type: 'SetOutputStructureComment';
  structureComment: string | null;
}

export type Mutation =
  // Experiment mutations
  | SetBatchCreatorMutation
  // Reaction mutations
  | SetSchemeMutation
  | ResolveInputsMutation
  | AddEmptyInputMutation
  | AddInput
  | RemoveInputMutation
  // Input mutations
  | SetInputRowRole
  | SetInputRowMol
  | SetInputRowLimiting
  | SetInputRowSaltCode
  | SetInputRowSaltEQ
  | SetInputRowEQ
  | SetInputCompoundStereoisomerCode
  | SetInputCompoundMolWeight
  // Input sample mutations
  | SetInputDensity
  | SetInputMolarity
  | SetInputVolume
  | SetInputPurity
  | SetInputHealthHazards
  | SetInputMol
  | SetInputWeight
  | SetInputComment
  // Output mutations
  | AddProductSample
  | SetOutputRowType
  | SetOutputRowSaltCode
  | SetOutputRowSaltEQ
  | SetOutputRowEQ
  | SetOutputRowName
  | SetOutputRowChemicalName
  | SetOutputCompoundStereoisomerCode
  | SetOutputCompoundMolWeight
  // Output sample mutations
  | SetOutputDensity
  | SetOutputMolarity
  | SetOutputVolume
  | SetOutputPurity
  | SetOutputHealthHazards
  | SetOutputActualMol
  | SetOutputActualWeight
  | RegisterSample
  | SetOutputHandlingPrecautions
  | SetOutputStorageInstructions
  | SetOutputCompoundProtection
  | SetOutputSolubilityInSolvents
  | SetOutputResidualSolvents
  | SetOutputMeltingPoint
  | SetOutputPurityCalculations
  | SetOutputExternalSupplier
  | SetOutputSource
  | SetOutputSourceDetails
  | SetOutputComponentState
  | SetOutputBatchComment
  | SetOutputStructureComment;
