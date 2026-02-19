// TODO remove this mock when real data is fully available from backend

import {
  ReactionOutput,
  ReactionOutputSample,
} from '@core/types/entities/experiments/experiment.i';
import {
  VolumeUnit,
  WeightUnit,
  MolWeightUnit,
  MolUnit,
  NoUnit,
  ReactionOutputType,
  SampleRegistrationStatus,
  DensityUnit,
  SolubidityType,
  SolubidityQualitativeType,
  ComparisonOperator,
  PurityCalculationType,
} from '@core/types/entities/experiments/experiment-shared.i';
import { CompoundType } from '@core/types/entities/compound.i';

interface OutputSampleRow {
  output: ReactionOutput;
  sample: ReactionOutputSample;
}

// Mock output 1: Final Product (P0)
// Using real compound ID from backend: d42b55d2-0bba-4678-9df7-a1fe672a8cad
const mockOutput1: ReactionOutput = {
  anchor: 'b31dbcab-d5cf-4dc1-863b-3194247069f8',
  outputName: 'P0',
  compound: {
    compoundID: 'd42b55d2-0bba-4678-9df7-a1fe672a8cad',
    molFile: '',
    name: 'Final Product',
    casNumber: '',
    type: CompoundType.VIRTUAL,
    stereoisomerCode: {
      id: 'stereo-r-001',
      name: '(R)',
    },
    saltCode: {
      id: 'salt-hcl-001',
      code: 'HCl',
      name: 'Hydrochloride',
      formula: 'HCl',
      charge: -1,
      molWeight: 36.46,
    },
    saltEQ: 0.5,
    strCode: 'P0',
    compoundKey: 'P0',
    formula: 'C9H8O4',
    molWeight: { value: 362.33, unit: MolWeightUnit.G_PER_MOL, source: 1 },
    exactMass: 180.04,
    calculatedBatchMF: 'C9 H8 O4 * 0.5 (Cl⁻)',
  },
  type: ReactionOutputType.FINAL,
  eq: { value: 1, unit: NoUnit.NO_UNIT, source: 1 },
  theoMol: { value: 0.6488, unit: MolUnit.MOL, source: 1 },
  theoWeight: { value: 235.10, unit: WeightUnit.G, source: 1 },
  samples: [],
};

// Mock output 2: By-Product (P1)
// Using real compound ID from backend: 79a94896-a218-44ae-a3e9-53314bc129df
const mockOutput2: ReactionOutput = {
  anchor: '417e6a26-1a32-4f1f-91cf-a94815379345',
  outputName: 'P1',
  compound: {
    compoundID: '79a94896-a218-44ae-a3e9-53314bc129df',
    molFile: '',
    name: 'Acetic Acid',
    casNumber: '',
    type: CompoundType.STORED,
    strCode: 'P1',
    compoundKey: 'P1',
    formula: 'C2H4O2',
    molWeight: { value: 60.05, unit: MolWeightUnit.G_PER_MOL, source: 1 },
    exactMass: 60.02,
    calculatedBatchMF: 'C2 H4 O2',
  },
  type: ReactionOutputType.BY_PRODUCT,
  eq: { value: 1, unit: NoUnit.NO_UNIT, source: 1 },
  theoMol: { value: 0.6488, unit: MolUnit.MOL, source: 1 },
  theoWeight: { value: 38.96, unit: WeightUnit.G, source: 1 },
  samples: [],
};

// Mock sample 1: For Final Product
const mockSample1: ReactionOutputSample = {
  anchor: 'mock-sample-1',
  nbkBatchNumber: '88888888-0001-P0-001',
  strCode: {
    compoundCode: 88888888,
    saltCode: 1,
    sampleCode: 1,
    stringForm: '88888888-0001-P0-001',
  },
  actualWeight: { value: 2.46, unit: WeightUnit.MG, source: 1 },
  volume: { value: 0.5, unit: VolumeUnit.ML, source: 1 },
  actualMol: { value: 0.015, unit: MolUnit.MMOL, source: 1 },
  density: { value: 1.05, unit: DensityUnit.G_ML, source: 1 },
  yield: { value: 0.01, unit: NoUnit.NO_UNIT, source: 1 },
  purity: { value: 0.95, unit: NoUnit.NO_UNIT, source: 1 },
  registrationStatus: SampleRegistrationStatus.REGISTERED,
  healthHazards: [
    { id: 'h1', name: 'Flammable' },
    { id: 'h2', name: 'Irritant' },
  ],
  handlingPrecautions: [
    { id: 'hp1', name: 'Use in fume hood' },
  ],
  storageInstructions: [
    { id: 'si1', name: 'Store at room temperature' },
  ],
  compoundProtection: [
    { id: 'cp1', name: 'Nitrogen atmosphere' },
  ],
  solubilityInSolvents: [
    {
      solvent: { id: 's1', name: 'Methanol' },
      solubidityType: SolubidityType.QUALITATIVE,
      qualitativeType: SolubidityQualitativeType.SOLUBLE,
    },
  ],
  residualSolvents: [
    { solvent: { id: 's2', name: 'Ethanol' }, eq: 0.5 },
  ],
  purityCalculations: [
    { type: PurityCalculationType.HPLC, operator: ComparisonOperator.GREATER_THAN, purity: 95 },
  ],
  meltingPoint: { lower: 45, upper: 48 },
  externalSupplier: {
    supplier: { id: 'sup1', name: 'Sigma-Aldrich' },
    registryNumber: 'SA-12345',
  },
  source: { id: 'src1', name: 'Commercial' },
  sourceDetails: { id: 'sd1', name: 'Purchased' },
  componentState: { id: 'cs1', name: 'Solid' },
  batchComment: 'High purity batch',
  structureComment: 'Confirmed by NMR',
} as ReactionOutputSample;

// Mock sample 2: For By-Product (using real sample data from backend)
const mockSample2: ReactionOutputSample = {
  anchor: '5e3aadd0-c9a1-4d02-a54c-11e56e790a3b',
  nbkBatchNumber: '88888888-0001-001',
  strCode: {
    compoundCode: 88888888,
    saltCode: 1,
    sampleCode: 1,
    stringForm: '88888888-0001-001',
  },
  actualWeight: { value: 10.0, unit: WeightUnit.G, source: 1 },
  actualMol: { value: 200.0, unit: MolUnit.MMOL, source: 1 },
  yield: { value: 0.3082, unit: NoUnit.NO_UNIT, source: 1 },
  purity: { value: 0.5, unit: NoUnit.NO_UNIT, source: 1 },
  registrationStatus: SampleRegistrationStatus.REGISTERED,
  healthHazards: [],
  handlingPrecautions: [],
  storageInstructions: [],
  compoundProtection: [],
  solubilityInSolvents: [],
  residualSolvents: [],
  purityCalculations: [],
} as ReactionOutputSample;

/**
 * Mock output samples for testing/development
 * Uses real compound IDs from backend to enable structure image loading
 */
export const MOCK_OUTPUT_SAMPLES: OutputSampleRow[] = [
  {
    output: mockOutput1,
    sample: mockSample1,
  },
  {
    output: mockOutput2,
    sample: mockSample2,
  },
];
