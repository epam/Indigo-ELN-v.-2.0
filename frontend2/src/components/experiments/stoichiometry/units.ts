import type {
  DensityUnit,
  MolarityUnit,
  MolUnit,
  MolWeightUnit,
  NoUnit,
  VolumeUnit,
  WeightUnit,
} from '@/lib/types/reactions.ts';

/**
 * How each unit is written. Mirrors the `displayName` on the backend's `MeasurementUnit`
 * enums — the wire carries the enum name (`MMOL`) and never the label (`mmol`), so this is
 * the only place the two are tied together.
 *
 * One flat map over every unit type, matching `MeasurementUnit.ALL_UNITS`: the names are
 * unique across all seven enums, and a per-type map would make `unitLabel` generic for no
 * gain. `NO_UNIT` is the empty string, so a unitless value renders as a bare number.
 */
const UNIT_LABELS: Record<string, string> = {
  UMOL: 'μmol',
  MMOL: 'mmol',
  MOL: 'mol',
  MG: 'mg',
  G: 'g',
  KG: 'kg',
  ML: 'mL',
  L: 'L',
  MM: 'mM',
  M: 'M',
  G_ML: 'g/mL',
  G_PER_MOL: 'g/mol',
  NO_UNIT: '',
};

export function unitLabel(unit: string | undefined): string {
  return unit === undefined ? '' : (UNIT_LABELS[unit] ?? unit);
}

/**
 * The options a unit picker offers, in the order the backend enum declares them (ascending
 * magnitude). `as const` on each so a column can state which list it takes and have the
 * element type flow through to the mutation it builds.
 */
export const MOL_UNITS = ['UMOL', 'MMOL', 'MOL'] as const satisfies readonly MolUnit[];
export const WEIGHT_UNITS = ['MG', 'G', 'KG'] as const satisfies readonly WeightUnit[];
export const VOLUME_UNITS = ['ML', 'L'] as const satisfies readonly VolumeUnit[];
export const MOLARITY_UNITS = ['MM', 'M'] as const satisfies readonly MolarityUnit[];
export const DENSITY_UNITS = ['G_ML'] as const satisfies readonly DensityUnit[];
export const MOL_WEIGHT_UNITS = ['G_PER_MOL'] as const satisfies readonly MolWeightUnit[];

/**
 * A unitless quantity — EQ, purity, salt EQ. The model still types these `EnteredValue<NoUnit>`
 * and the backend still expects `NO_UNIT` back, so they are a one-option list rather than a
 * separate cell kind: the picker hides itself when there is nothing to choose between.
 */
export const NO_UNITS = ['NO_UNIT'] as const satisfies readonly NoUnit[];
