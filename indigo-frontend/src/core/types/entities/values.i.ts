export class MeasurementUnit {
  constructor(
    public readonly value: string,
    public readonly displayName: string,
  ) {}
}

export const NO_UNITS: MeasurementUnit[] = [];

export const MOL_UNITS = [
  new MeasurementUnit('UMOL', 'μmol'),
  new MeasurementUnit('MMOL', 'mmol'),
  new MeasurementUnit('MOL', 'mol'),
];

export const DENSITY_UNITS = [new MeasurementUnit('G_ML', 'g/mL')];

export const MOLARITY_UNITS = [
  new MeasurementUnit('MM', 'mM'),
  new MeasurementUnit('M', 'M'),
];

export const VOLUME_UNITS = [
  new MeasurementUnit('ML', 'mL'),
  new MeasurementUnit('L', 'L'),
];

export const WEIGHT_UNITS = [
  new MeasurementUnit('MG', 'mg'),
  new MeasurementUnit('G', 'g'),
  new MeasurementUnit('KG', 'kg'),
];

export const MOL_WEIGHT_UNITS = [new MeasurementUnit('G_PER_MOL', 'g/mol')];

export enum EnteredValueSource {
  FIXED = 'FIXED',
  USER_LAST_ENTERED = 'USER_LAST_ENTERED',
  USER_ENTERED = 'USER_ENTER',
  CALCULATED_FROM_LAST_ENTERED = 'CALCULATED_FROM_LAST_ENTERED',
  CALCULATED = 'CALCULATED',
  DEFAULT = 'DEFAULT',
}

export interface EnteredValue<U> {
  value: string;
  unit: U;
  source: EnteredValueSource | number | 'fixed' | 'default';
  overwritten?: boolean;
}

export const REACTION_INPUT_ROLES = [
  { id: 'REACTANT', name: 'Reactant' },
  { id: 'CATALYST', name: 'Catalyst' },
  { id: 'SOLVENT', name: 'Solvent' },
];
