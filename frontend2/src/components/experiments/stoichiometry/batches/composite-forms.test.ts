import { describe, expect, it } from 'vitest';

import {
  areResidualSolventRowsValid,
  areSolubilityRowsValid,
  EMPTY_RESIDUAL_SOLVENT_ROW,
  EMPTY_SOLUBILITY_ROW,
  isSameValue,
  toMeltingPoint,
  toMeltingPointForm,
  toResidualSolventRows,
  toResidualSolvents,
  toSolubilityInSolvents,
  toSolubilityRows,
} from '@/components/experiments/stoichiometry/batches/composite-forms';

import type { ResidualSolvent, SolubidityInSolvent } from '@/lib/types/reactions.ts';

const TOLUENE = { id: 'a0000000-0000-4000-8000-000000000001', name: 'Toluene' };
const WATER = { id: 'a0000000-0000-4000-8000-000000000002', name: 'Water' };

describe('melting point', () => {
  it('round-trips a full value', () => {
    const saved = { lower: 67, upper: 69.5, comments: 'decomposes' };
    expect(toMeltingPointForm(saved)).toEqual({ lower: '67', upper: '69.5', comments: 'decomposes' });
    expect(toMeltingPoint(toMeltingPointForm(saved))).toEqual(saved);
  });

  it('keeps a zero and a negative bound', () => {
    expect(toMeltingPoint({ lower: '-10', upper: '0', comments: '' })).toEqual({ lower: -10, upper: 0 });
  });

  it('is null when nothing is filled in', () => {
    expect(toMeltingPoint(toMeltingPointForm(undefined))).toBeNull();
    expect(toMeltingPoint({ lower: '', upper: '', comments: '   ' })).toBeNull();
  });

  it('keeps a comment on its own, trimmed', () => {
    expect(toMeltingPoint({ lower: '', upper: '', comments: ' sublimes ' })).toEqual({ comments: 'sublimes' });
  });
});

describe('residual solvents', () => {
  it('opens an empty list on one blank row', () => {
    expect(toResidualSolventRows([])).toEqual([EMPTY_RESIDUAL_SOLVENT_ROW]);
  });

  it('round-trips', () => {
    const saved: ResidualSolvent[] = [
      { solvent: TOLUENE, eq: 1.2, comment: 'trace' },
      { solvent: WATER, eq: 0.5 },
    ];
    expect(toResidualSolvents(toResidualSolventRows(saved))).toEqual(saved);
  });

  it('drops the rows nobody filled in', () => {
    const rows = [EMPTY_RESIDUAL_SOLVENT_ROW, { solvent: TOLUENE, eq: '2', comment: '' }, EMPTY_RESIDUAL_SOLVENT_ROW];
    expect(areResidualSolventRowsValid(rows)).toBe(true);
    expect(toResidualSolvents(rows)).toEqual([{ solvent: TOLUENE, eq: 2 }]);
  });

  it('refuses a row missing its solvent or its EQ', () => {
    expect(areResidualSolventRowsValid([{ solvent: TOLUENE, eq: '', comment: '' }])).toBe(false);
    expect(areResidualSolventRowsValid([{ solvent: null, eq: '1', comment: '' }])).toBe(false);
    expect(areResidualSolventRowsValid([{ solvent: null, eq: '', comment: 'note' }])).toBe(false);
  });
});

describe('solubility in solvents', () => {
  it('opens an empty list on one blank quantitative row', () => {
    expect(toSolubilityRows([])).toEqual([EMPTY_SOLUBILITY_ROW]);
  });

  it('round-trips both types', () => {
    const saved: SolubidityInSolvent[] = [
      { type: 'QUANTITATIVE', solvent: WATER, operator: 'LESS_THAN', value: 5, unit: 'G_ML', comment: 'cold' },
      { type: 'QUALITATIVE', solvent: TOLUENE, qualitativeType: 'SOLUBLE' },
    ];
    expect(toSolubilityInSolvents(toSolubilityRows(saved))).toEqual(saved);
  });

  it('sends neither operator nor unit without a number', () => {
    const rows = [{ ...EMPTY_SOLUBILITY_ROW, solvent: WATER, operator: 'EQUALS' as const }];
    expect(toSolubilityInSolvents(rows)).toEqual([{ type: 'QUANTITATIVE', solvent: WATER }]);
  });

  it('sends only the current type’s fields after a switch', () => {
    const rows = [
      { ...EMPTY_SOLUBILITY_ROW, solvent: WATER, value: '5', type: 'QUALITATIVE' as const },
      { ...EMPTY_SOLUBILITY_ROW, solvent: TOLUENE, value: '5', qualitativeType: 'PRECIPITATE' as const },
    ];
    expect(toSolubilityInSolvents(rows)).toEqual([
      { type: 'QUALITATIVE', solvent: WATER },
      { type: 'QUANTITATIVE', solvent: TOLUENE, operator: 'GREATER_THAN', value: 5, unit: 'G_ML' },
    ]);
  });

  it('drops blank rows, whatever their type, and refuses a filled one with no solvent', () => {
    const blank = { ...EMPTY_SOLUBILITY_ROW, type: 'QUALITATIVE' as const, operator: 'EQUALS' as const };
    expect(areSolubilityRowsValid([blank])).toBe(true);
    expect(toSolubilityInSolvents([blank])).toEqual([]);
    expect(areSolubilityRowsValid([{ ...EMPTY_SOLUBILITY_ROW, value: '5' }])).toBe(false);
    expect(areSolubilityRowsValid([{ ...EMPTY_SOLUBILITY_ROW, qualitativeType: 'SOLUBLE' }])).toBe(false);
  });
});

describe('isSameValue', () => {
  it('sees an untouched editor as unchanged', () => {
    const saved: ResidualSolvent[] = [{ solvent: TOLUENE, eq: 1.2 }];
    expect(isSameValue(toResidualSolvents(toResidualSolventRows(saved)), saved)).toBe(true);
    expect(isSameValue(toResidualSolvents([{ solvent: TOLUENE, eq: '1.3', comment: '' }]), saved)).toBe(false);
  });
});
