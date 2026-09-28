import { describe, expect, it } from 'vitest';

import {
  externalSupplierLabel,
  meltingPointLabel,
  purityCalculationLabels,
  residualSolventLabels,
  solubilityLabels,
} from '@/components/experiments/stoichiometry/batches/detail';

import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';

const WATER: DictionaryItemRef = { id: '00000000-0000-4000-8000-000000000001', name: 'Water' };

describe('meltingPointLabel', () => {
  it('joins both bounds into a range', () => {
    expect(meltingPointLabel({ lower: 67, upper: 69 })).toBe('67 ~ 69 °C');
  });

  it('shows a single bound on its own', () => {
    expect(meltingPointLabel({ lower: 67 })).toBe('67 °C');
    expect(meltingPointLabel({ upper: 69 })).toBe('69 °C');
  });

  it('appends the comment to a range', () => {
    expect(meltingPointLabel({ lower: 67, upper: 69, comments: 'decomposes' })).toBe('67 ~ 69 °C (decomposes)');
  });

  // Every member is optional, so a record carrying nothing but a comment is on the wire.
  it('falls back to the comment when neither bound was recorded', () => {
    expect(meltingPointLabel({ comments: 'not measured' })).toBe('not measured');
  });

  it('has nothing to say for an absent or empty record', () => {
    expect(meltingPointLabel(undefined)).toBeUndefined();
    expect(meltingPointLabel({})).toBeUndefined();
  });
});

describe('residualSolventLabels', () => {
  it('carries the equivalents, which are the point of the record', () => {
    expect(residualSolventLabels([{ solvent: WATER, eq: 1.2 }])).toEqual(['Water (1.2 eq)']);
  });

  it('is empty for an absent list', () => {
    expect(residualSolventLabels(undefined)).toEqual([]);
  });
});

describe('solubilityLabels', () => {
  it('writes a quantitative entry with its operator and unit', () => {
    expect(
      solubilityLabels([{ type: 'QUANTITATIVE', solvent: WATER, operator: 'GREATER_THAN', value: 5, unit: 'G_ML' }]),
    ).toEqual(['Water > 5 g/mL']);
  });

  it('drops the operator when none was chosen', () => {
    expect(solubilityLabels([{ type: 'QUANTITATIVE', solvent: WATER, value: 5, unit: 'G_ML' }])).toEqual([
      'Water 5 g/mL',
    ]);
  });

  it('names the solvent alone when a quantitative entry has no value yet', () => {
    expect(solubilityLabels([{ type: 'QUANTITATIVE', solvent: WATER }])).toEqual(['Water']);
  });

  it('writes a qualitative entry as its verdict', () => {
    expect(solubilityLabels([{ type: 'QUALITATIVE', solvent: WATER, qualitativeType: 'UNSOLUBLE' }])).toEqual([
      'Water: Insoluble',
    ]);
  });

  it('names the solvent alone when a qualitative entry has no verdict yet', () => {
    expect(solubilityLabels([{ type: 'QUALITATIVE', solvent: WATER }])).toEqual(['Water']);
  });

  it('is empty for an absent list', () => {
    expect(solubilityLabels(undefined)).toEqual([]);
  });
});

describe('externalSupplierLabel', () => {
  it('pairs the supplier with its registry number', () => {
    expect(externalSupplierLabel({ supplier: WATER, registryNumber: 'A1234' })).toBe('Water (A1234)');
  });

  it('shows the supplier alone when the registry number is blank', () => {
    expect(externalSupplierLabel({ supplier: WATER, registryNumber: '' })).toBe('Water');
  });

  it('has nothing to say for an absent supplier', () => {
    expect(externalSupplierLabel(undefined)).toBeUndefined();
  });
});

describe('purityCalculationLabels', () => {
  it('writes the method, the operator and the number', () => {
    expect(purityCalculationLabels([{ type: 'HPLC', operator: 'GREATER_THAN', purity: 98 }])).toEqual(['HPLC > 98']);
  });

  it('is empty for an absent list', () => {
    expect(purityCalculationLabels(undefined)).toEqual([]);
  });
});
