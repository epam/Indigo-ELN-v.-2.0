import { describe, expect, it } from 'vitest';

import type { ExperimentModel } from '@/lib/types/reactions.ts';
import { columnUnitLabel } from '@/lib/types/reactions.ts';

describe('columnUnitLabel', () => {
  const model: ExperimentModel = {
    reactions: [],
    significantFigures: 5,
    unitSource: {
      weightUnit: 'MG',
      volumeUnit: 'L',
      molUnit: 'MMOL',
      molarityUnit: 'MM',
    },
  };

  it("reads the experiment's chosen units", () => {
    expect(columnUnitLabel(model, 'weight')).toBe('mg');
    expect(columnUnitLabel(model, 'volume')).toBe('L');
    expect(columnUnitLabel(model, 'mol')).toBe('mmol');
    expect(columnUnitLabel(model, 'molarity')).toBe('mM');
  });

  it('writes the fixed units', () => {
    expect(columnUnitLabel(model, 'density')).toBe('g/mL');
    expect(columnUnitLabel(model, 'molWeight')).toBe('g/mol');
    expect(columnUnitLabel(model, 'percent')).toBe('%');
  });
});
