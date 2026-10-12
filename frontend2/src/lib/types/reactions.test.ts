import { describe, expect, it } from 'vitest';

import {
  convertUnitValue,
  MOL_UNITS,
  MOLARITY_UNITS,
  unitLabel,
  VOLUME_UNITS,
  WEIGHT_UNITS,
} from '@/lib/types/reactions.ts';

describe('convertUnitValue', () => {
  it('rewrites a weight in the unit asked for', () => {
    expect(convertUnitValue('676.5', 'MG', 'G')).toBe('0.6765');
    expect(convertUnitValue('676.5', 'MG', 'KG')).toBe('0.0006765');
    expect(convertUnitValue('0.6765', 'G', 'MG')).toBe('676.5');
    expect(convertUnitValue('5', 'G', 'MG')).toBe('5000');
  });

  it('does the same for the other kinds', () => {
    expect(convertUnitValue('4.9', 'MMOL', 'MOL')).toBe('0.0049');
    expect(convertUnitValue('4.9', 'MMOL', 'UMOL')).toBe('4900');
    expect(convertUnitValue('12', 'ML', 'L')).toBe('0.012');
    expect(convertUnitValue('0.5', 'M', 'MM')).toBe('500');
  });

  /**
   * The reason the digits are moved rather than multiplied: `4.9 * 1e-3` is
   * `0.0049000000000000002`, and this is a value the user reads back and saves.
   */
  it('is exact, and so survives a round trip', () => {
    expect(convertUnitValue(convertUnitValue('4.9', 'MMOL', 'MOL'), 'MOL', 'MMOL')).toBe('4.9');
    expect(convertUnitValue(convertUnitValue('676.5', 'MG', 'KG'), 'KG', 'MG')).toBe('676.5');
    expect(convertUnitValue('0.1', 'MG', 'G')).toBe('0.0001');
  });

  it('keeps the sign and drops only the zeros it padded with', () => {
    expect(convertUnitValue('-676.5', 'MG', 'G')).toBe('-0.6765');
    expect(convertUnitValue('1.50', 'MG', 'G')).toBe('0.0015');
    expect(convertUnitValue('0100', 'MG', 'G')).toBe('0.1');
  });

  it('is a no-op between units of the same size', () => {
    expect(convertUnitValue('42', 'G', 'G')).toBe('42');
    expect(convertUnitValue('42', 'NO_UNIT', 'NO_UNIT')).toBe('42');
  });

  it('leaves alone anything it cannot take apart', () => {
    // A unit it does not know — a new backend member, or a typo.
    expect(convertUnitValue('676.5', 'MG', 'STONE')).toBe('676.5');
    expect(convertUnitValue('676.5', 'OUNCE', 'G')).toBe('676.5');
    // `type="number"` accepts these, and neither is a plain decimal.
    expect(convertUnitValue('1e3', 'MG', 'G')).toBe('1e3');
    expect(convertUnitValue('', 'MG', 'G')).toBe('');
    expect(convertUnitValue('-', 'MG', 'G')).toBe('-');
  });

  /**
   * A unit with no multiplier would convert to itself in silence, which reads as the feature
   * quietly not working. Every unit a picker offers is covered.
   */
  it('knows every unit the pickers offer', () => {
    for (const units of [WEIGHT_UNITS, VOLUME_UNITS, MOL_UNITS, MOLARITY_UNITS]) {
      for (const unit of units) {
        const other = units.find((candidate) => candidate !== unit);
        if (other) expect(convertUnitValue('1', unit, other), `${unitLabel(unit)} → ${unitLabel(other)}`).not.toBe('1');
      }
    }
  });
});
