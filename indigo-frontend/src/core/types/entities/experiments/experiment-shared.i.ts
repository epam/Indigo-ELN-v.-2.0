// Shared types used across multiple experiment-related schemas
// Auto-generated from OpenAPI schemas
// Source: /mnt/data/swagger spec

// ================================
// 1. BASE TYPES
// ================================
import { DictionaryItemRef } from '@core/types/entities/dictionary.i';

export type UUID = string;

// ================================
// 2. ENUMS - UNITS
// ================================
export enum MolUnit {
  UMOL = 'UMOL',
  MMOL = 'MMOL',
  MOL = 'MOL',
}

export enum WeightUnit {
  MG = 'MG',
  G = 'G',
  KG = 'KG',
}

export enum VolumeUnit {
  ML = 'ML',
  L = 'L',
}

export enum DensityUnit {
  G_ML = 'G_ML',
}

export enum MolarityUnit {
  MM = 'MM',
  M = 'M',
}

export enum MolWeightUnit {
  G_PER_MOL = 'G_PER_MOL',
}

export enum NoUnit {
  NO_UNIT = 'NO_UNIT',
}

// ================================
// 3. ENUMS - DOMAIN SPECIFIC
// ================================
export enum ReactionRole {
  REACTANT = 'REACTANT',
  CATALYST = 'CATALYST',
  SOLVENT = 'SOLVENT',
  OUTPUT = 'OUTPUT',
}

export const ReactionRoleNames: Record<ReactionRole, string> = {
  REACTANT: 'Reactant',
  CATALYST: 'Catalyst',
  SOLVENT: 'Solvent',
  OUTPUT: 'Output',
};

export enum ReactionOutputType {
  FINAL = 'FINAL',
  BY_PRODUCT = 'BY_PRODUCT',
  INTERMEDIATE = 'INTERMEDIATE',
}

export enum SampleRegistrationStatus {
  IN_PROGRESS = 'IN_PROGRESS',
  FAILED = 'FAILED',
  REGISTERED = 'REGISTERED',
}

export enum EnteredValueSource {
  FIXED = 'FIXED',
  USER_LAST_ENTERED = 'USER_LAST_ENTERED',
  USER_ENTERED = 'USER_ENTERED',
  CALCULATED_FROM_LAST_ENTERED = 'CALCULATED_FROM_LAST_ENTERED',
  CALCULATED = 'CALCULATED',
  DEFAULT = 'DEFAULT',
}

// ================================
// 4. ENUMS - CALCULATIONS & OPERATORS
// ================================
export enum ComparisonOperator {
  GREATER_THAN = 'GREATER_THAN',
  LESS_THAN = 'LESS_THAN',
  EQUALS = 'EQUALS',
  APPROXIMATELY = 'APPROXIMATELY',
}

export enum PurityCalculationType {
  NMR = 'NMR',
  HPLC = 'HPLC',
  LCMS = 'LCMS',
  CHN = 'CHN',
  MS = 'MS',
}

export enum SolubidityType {
  QUANTITATIVE = 'QUANTITATIVE',
  QUALITATIVE = 'QUALITATIVE',
}

export enum SolubidityQualitativeType {
  SOLUBLE = 'SOLUBLE',
  UNSOLUBLE = 'UNSOLUBLE',
  PRECIPITATE = 'PRECIPITATE',
}

// ================================
// 5. UTILITY INTERFACES - ENTERED VALUES
// ================================
export interface EnteredValueMolUnit {
  value?: number;
  unit?: MolUnit;
  source?: EnteredValueSource;
  conflict?: boolean;
}

export interface EnteredValueWeightUnit {
  value?: number;
  unit?: WeightUnit;
  source?: EnteredValueSource;
  conflict?: boolean;
}

export interface EnteredValueVolumeUnit {
  value?: number;
  unit?: VolumeUnit;
  source?: EnteredValueSource;
  conflict?: boolean;
}

export interface EnteredValueDensityUnit {
  value?: number;
  unit?: DensityUnit;
  source?: EnteredValueSource;
  conflict?: boolean;
}

export interface EnteredValueMolarityUnit {
  value?: number;
  unit?: MolarityUnit;
  source?: EnteredValueSource;
  conflict?: boolean;
}

export interface EnteredValueMolWeightUnit {
  value?: number;
  unit?: MolWeightUnit;
  source?: EnteredValueSource;
  conflict?: boolean;
}

export interface EnteredValueNoUnit {
  value?: number;
  unit?: NoUnit;
  source?: EnteredValueSource;
  conflict?: boolean;
}

// ================================
// 6. REFERENCE INTERFACES
// ================================
export interface STRCodeCompound {
  compoundCode?: number;
  saltCode?: number;
  stringForm?: string;
}

export interface STRCodeSample {
  compoundCode?: number;
  saltCode?: number;
  sampleCode?: number;
  stringForm?: string;
}

// ================================
// 7. PROPERTY INTERFACES - SIMPLE
// ================================
export interface MeltingPoint {
  lower?: number;
  upper?: number;
  comments?: string;
}

export interface ExternalSupplier {
  supplier: DictionaryItemRef;
  registryNumber: string;
}

export interface PurityCalculation {
  type: PurityCalculationType;
  operator: ComparisonOperator;
  purity: number;
  comment?: string;
}

export interface SolubidityInSolvent {
  solvent: DictionaryItemRef;
  comment?: string;
  solubidityType: SolubidityType;
  operator?: ComparisonOperator;
  value?: number;
  unit?: DensityUnit;
  qualitativeType?: SolubidityQualitativeType;
}

export interface ResidualSolvent {
  solvent: DictionaryItemRef;
  eq: number;
  comment?: string;
}
