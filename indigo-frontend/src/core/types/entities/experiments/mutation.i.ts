import { ReactionInputRole } from './experiment-shared.i';
import { DictionaryItemRef } from '../dictionary.i';

// Base mutation interface
interface BaseMutation {
    anchor: string;
}

// Reaction mutations
interface SetSchemeMutation extends BaseMutation {
    type: 'SetScheme';
    molFile: string;
}

interface ResolveInputsMutation extends BaseMutation {
    type: 'ResolveInputs';
    inputSamples: Record<string, string>;
}

// Input mutations
interface SetInputRoleMutation extends BaseMutation {
    type: 'SetInputRole';
    role: ReactionInputRole;
}

interface SetLimitingMutation extends BaseMutation {
    type: 'SetLimiting';
}

interface SetInputSaltCodeMutation extends BaseMutation {
    type: 'SetInputSaltCode';
    saltCode: DictionaryItemRef | null;
}

interface SetInputSaltEQMutation extends BaseMutation {
    type: 'SetInputSaltEQ';
    saltEQ: number | null;
}

interface SetInputEQMutation extends BaseMutation {
    type: 'SetInputEQ';
    eq: number | null;
}

// Input sample mutations
interface SetInputDensityMutation extends BaseMutation {
    type: 'SetInputDensity';
    density: number | null;
    unit: string /*DensityUnit*/ | null;
}

interface SetInputMolarityMutation extends BaseMutation {
    type: 'SetInputMolarity';
    molarity: number | null;
    unit: string /*MolarityUnit*/ | null;
}

interface SetInputVolumeMutation extends BaseMutation {
    type: 'SetInputVolume';
    volume: number | null;
    unit: string /*VolumeUnit*/ | null;
}

interface SetInputPurityMutation extends BaseMutation {
    type: 'SetInputPurity';
    purity: number | null;
}

interface SetInputMolMutation extends BaseMutation {
    type: 'SetInputMol';
    mol: number | null;
    unit: string /*MolUnit*/ | null;
}

interface SetInputWeightMutation extends BaseMutation {
    type: 'SetInputWeight';
    weight: number | null;
    unit: string /*WeightUnit*/ | null;
}

// Output mutations
interface AddProductSampleMutation extends BaseMutation {
    type: 'AddProductSample';
}

interface SetOutputSaltCodeMutation extends BaseMutation {
    type: 'SetOutputSaltCode';
    saltCode: DictionaryItemRef | null;
}

interface SetOutputSaltEQMutation extends BaseMutation {
    type: 'SetOutputSaltEQ';
    saltEQ: number | null;
}

interface SetOutputEQMutation extends BaseMutation {
    type: 'SetOutputEQ';
    eq: number | null;
}

// Output sample mutations
interface SetOutputDensityMutation extends BaseMutation {
    type: 'SetOutputDensity';
    density: number | null;
    unit: string /*DensityUnit*/ | null;
}

interface SetOutputMolarityMutation extends BaseMutation {
    type: 'SetOutputMolarity';
    molarity: number | null;
    unit: string /*MolarityUnit*/ | null;
}

interface SetOutputVolumeMutation extends BaseMutation {
    type: 'SetOutputVolume';
    volume: number | null;
    unit: string /*VolumeUnit*/ | null;
}

interface SetOutputPurityMutation extends BaseMutation {
    type: 'SetOutputPurity';
    purity: number | null;
}

interface SetOutputActualMolMutation extends BaseMutation {
    type: 'SetOutputActualMol';
    actualMol: number | null;
    unit: string /*MolUnit*/ | null;
}

interface SetOutputActualWeightMutation extends BaseMutation {
    type: 'SetOutputActualWeight';
    actualWeight: number | null;
    unit: string /*WeightUnit*/ | null;
}

// Sample mutations
interface RegisterSampleMutation extends BaseMutation {
    type: 'RegisterSample';
}

export type Mutation =
    // Reaction mutations
    | SetSchemeMutation
    | ResolveInputsMutation
    // Input mutations
    | SetInputRoleMutation
    | SetLimitingMutation
    | SetInputSaltCodeMutation
    | SetInputSaltEQMutation
    | SetInputEQMutation
    // Input sample mutations
    | SetInputDensityMutation
    | SetInputMolarityMutation
    | SetInputVolumeMutation
    | SetInputPurityMutation
    | SetInputMolMutation
    | SetInputWeightMutation
    // Output mutations
    | AddProductSampleMutation
    | SetOutputSaltCodeMutation
    | SetOutputSaltEQMutation
    | SetOutputEQMutation
    // Output sample mutations
    | SetOutputDensityMutation
    | SetOutputMolarityMutation
    | SetOutputVolumeMutation
    | SetOutputPurityMutation
    | SetOutputActualMolMutation
    | SetOutputActualWeightMutation
    // Sample mutations
    | RegisterSampleMutation;

// Uncommented future mutations for reference
// | { type: 'AddInput' }
// | { type: 'RemoveInput', anchor: string }
// | { type: 'SetOutputType', anchor: string, type: ReactionOutputType }