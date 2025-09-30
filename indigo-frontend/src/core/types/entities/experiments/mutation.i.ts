import { ReactionInputRole } from './experiment-shared.i';
import { DictionaryItemRef } from '../dictionary.i';

export type Mutation =
    // reaction
    | {
        type: 'SetScheme';
        anchor: string;
    }
    | {
        type: 'ResolveInputs';
        anchor: string;
        inputSamples: Record<string, string>;
    }
    // | { type: 'AddInput' }
    // | { type: 'RemoveInput', anchor: string }
    // reaction input
    | {
        type: 'SetInputRole';
        anchor: string;
        role: ReactionInputRole;
    }
    | {
        type: 'SetLimiting';
        anchor: string;
    }
    | {
        type: 'SetInputSaltCode';
        anchor: string;
        saltCode: DictionaryItemRef | null;
    }
    | {
        type: 'SetInputSaltEQ';
        anchor: string;
        saltEQ: number | null;
    }
    | {
        type: 'SetInputEQ';
        anchor: string;
        eq: number | null;
    }
    // reaction input sample
    | {
        type: 'SetInputDensity';
        anchor: string;
        density: number | null;
        unit: string /*DensityUnit*/ | null;
    }
    | {
        type: 'SetInputMolarity';
        anchor: string;
        molarity: number | null;
        unit: string /*MolarityUnit*/ | null;
    }
    | {
        type: 'SetInputVolume';
        anchor: string;
        volume: number | null;
        unit: string /*VolumeUnit*/ | null;
    }
    | {
        type: 'SetInputPurity';
        anchor: string;
        purity: number | null;
    }
    | {
        type: 'SetInputMol';
        anchor: string;
        mol: number | null;
        unit: string /*MolUnit*/ | null;
    }
    | {
        type: 'SetInputWeight';
        anchor: string;
        weight: number | null;
        unit: string /*WeightUnit*/ | null;
    }
    // reaction output
    | {
        type: 'AddProductSample';
        anchor: string;
    }
    // | { type: 'SetOutputType', anchor: string, type: ReactionOutputType }
    | {
        type: 'SetOutputSaltCode';
        anchor: string;
        saltCode: DictionaryItemRef | null;
    }
    | {
        type: 'SetOutputSaltEQ';
        anchor: string;
        saltEQ: number | null;
    }
    | {
        type: 'SetOutputEQ';
        anchor: string;
        eq: number | null;
    }
    // reaction output sample
    | {
        type: 'SetOutputDensity';
        anchor: string;
        density: number | null;
        unit: string /*DensityUnit*/ | null;
    }
    | {
        type: 'SetOutputMolarity';
        anchor: string;
        molarity: number | null;
        unit: string /*MolarityUnit*/ | null;
    }
    | {
        type: 'SetOutputVolume';
        anchor: string;
        volume: number | null;
        unit: string /*VolumeUnit*/ | null;
    }
    | {
        type: 'SetOutputPurity';
        anchor: string;
        purity: number | null;
    }
    | {
        type: 'SetOutputActualMol';
        anchor: string;
        actualMol: number | null;
        unit: string /*MolUnit*/ | null;
    }
    | {
        type: 'SetOutputActualWeight';
        anchor: string;
        actualWeight: number | null;
        unit: string /*WeightUnit*/ | null;
    }
    | {
        type: 'RegisterSample';
        anchor: string;
    };