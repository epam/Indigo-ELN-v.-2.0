import type { UUID } from '@/lib/types/common.ts';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';

/**
 * `src/lib/types/reactions.ts` mirrors the backend `reaction/model` package — the JSON blob
 * stored in `experiment.model` and diffed by every model mutation.
 *
 * Ported from the Java. The wire shapes that are easy to get wrong — `NbkBatchNumber` is a
 * string because of `@JsonValue`, `SolubidityInSolvent` is a discriminated union, and
 * `EnteredValue.source` is not a named enum — are each noted again at the type they affect.
 *
 * Every `Anchor` subclass (`ReactionAnchor`, `InputAnchor`, …) serialises as a bare string, so
 * they are all `Anchor` here.
 */

/**
 * Mirrors `Anchor` and its subclasses: a row's or sample's id within its experiment — 8 base64url
 * characters (`newAnchor`), or a UUID in models written before the short form. Not a `UUID`.
 */
export type Anchor = string;

/* ── Units ─────────────────────────────────────────────────────────────────────────────── */

export type MolUnit = 'UMOL' | 'MMOL' | 'MOL';
export type WeightUnit = 'MG' | 'G' | 'KG';
export type VolumeUnit = 'ML' | 'L';
export type DensityUnit = 'G_ML';
/** `KM` is the backend's internal base unit for molarity, never offered as a choice. */
export type MolarityUnit = 'MM' | 'M' | 'KM';
export type MolWeightUnit = 'G_PER_MOL';
export type NoUnit = 'NO_UNIT';

/** Every measurement unit, matching the backend's `UnitUtils.ALL_UNITS`. */
export type MeasurementUnit = MolUnit | WeightUnit | VolumeUnit | DensityUnit | MolarityUnit | MolWeightUnit | NoUnit;

/**
 * How each unit is written. Mirrors the `displayName` on the backend's `MeasurementUnit`
 * enums — the wire carries the enum name (`MMOL`) and never the label (`mmol`), so this is
 * the only place the two are tied together.
 *
 * One flat map over every unit type, matching `UnitUtils.ALL_UNITS`: the names are
 * unique across all seven enums, and a per-type map would make `unitLabel` generic for no
 * gain. `NO_UNIT` is the empty string, so a unitless value renders as a bare number.
 */
const UNIT_LABELS: Record<MeasurementUnit, string> = {
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
  KM: 'KM',
  G_ML: 'g/mL',
  G_PER_MOL: 'g/mol',
  NO_UNIT: '',
};

/**
 * Takes a bare `string` rather than `MeasurementUnit` because that is what a unit field
 * (`SolubidityInSolvent.unit`) is once it has been off the wire, and falls back to the raw enum name so an unmapped member
 * shows as itself rather than as a blank.
 */
export function unitLabel(unit: string | undefined): string {
  return unit === undefined ? '' : (UNIT_LABELS[unit as MeasurementUnit] ?? unit);
}

/**
 * The options the experiment's unit pickers offer, in the order the backend enum declares them
 * (ascending magnitude).
 */
export const MOL_UNITS = ['UMOL', 'MMOL', 'MOL'] as const satisfies readonly MolUnit[];
export const WEIGHT_UNITS = ['MG', 'G', 'KG'] as const satisfies readonly WeightUnit[];
export const VOLUME_UNITS = ['ML', 'L'] as const satisfies readonly VolumeUnit[];

/**
 * The unit a column's values are in, named by quantity rather than by unit — the header reads
 * it off the experiment (`columnUnitLabel`), so a column states *what* it measures and the model
 * says in which unit. Every value of a quantity is in that one unit, which is why no cell shows
 * one of its own.
 */
export type ColumnUnit = 'weight' | 'volume' | 'mol' | 'molarity' | 'density' | 'molWeight' | 'percent';

/** How a column's unit is written in its header: `g`, `mL`, `g/mol`, `%`. */
export function columnUnitLabel(model: ExperimentModel, unit: ColumnUnit): string {
  switch (unit) {
    case 'weight':
      return unitLabel(model.unitSource.weightUnit);
    case 'volume':
      return unitLabel(model.unitSource.volumeUnit);
    case 'mol':
      return unitLabel(model.unitSource.molUnit);
    case 'molarity':
      return unitLabel(model.unitSource.molarityUnit);
    case 'density':
      return unitLabel('G_ML');
    case 'molWeight':
      return unitLabel('G_PER_MOL');
    case 'percent':
      return '%';
  }
}

/**
 * Who set a value, as `EnteredValue.Serializer` spells it: `F` fixed (from the compound
 * registry), `D` default, `C` calculated, `U` user-entered and `L` user-cleared — the last two
 * carry the `revision` the user acted in.
 */
export type EnteredValueSource = 'F' | 'D' | 'C' | 'U' | 'L';

/**
 * One numeric cell of the stoichiometry model. Carries no unit: every value of a quantity is in
 * the experiment's one unit for it (`ExperimentModel.unitSource`). `source` is absent only for
 * an empty value, `value` is absent for an empty or user-cleared one, `exact` is set only on a
 * fixed value and `revision` only on a user one — so every field is optional.
 */
export interface EnteredValue {
  value?: string;
  exact?: number;
  source?: EnteredValueSource;
  revision?: number;
}

/* ── Compounds ─────────────────────────────────────────────────────────────────────────── */

/**
 * `MolFormula` serialises through `@JsonValue toHTMLString()`, so a formula arrives as
 * **HTML** — `C<sub>6</sub>H<sub>6</sub>`, not `C6H6`. Rendering it as plain text shows the
 * tags; it needs `dangerouslySetInnerHTML` or equivalent.
 */
export type MolFormula = string;

/**
 * The formula as text: `C<sub>6</sub>H<sub>6</sub>` → `C6H6`.
 *
 * Needed wherever a formula is used as something other than markup, and both of those are easy
 * to get wrong in the same silent way. **Searching** the raw string means the value can never be
 * found as it is displayed — typing `C6H6` matches nothing, while `sub` matches every row that
 * has a formula at all. **Announcing** it means a screen reader spells the tags out character by
 * character, which is why an `aria-label` or an `alt` naming a compound by its formula needs this
 * too. Only `FormulaCell` and the two `[&_sub]` spans want the markup itself.
 */
export function plainFormula(formula: MolFormula): string {
  return formula.replace(/<[^>]+>/g, '');
}

/** `NbkBatchNumber` is a `@JsonValue` string: `20240101-0001-003`. */
export type NbkBatchNumber = string;

/** Mirrors `NbkBatchNumber.getShortForm()`: the ordinal alone, still zero-padded to three. */
export function shortNbkBatchNumber(nbkBatchNumber: NbkBatchNumber): string {
  return nbkBatchNumber.slice(nbkBatchNumber.lastIndexOf('-') + 1);
}

/**
 * One flat class, `NON_NULL`: a compound is either **known** — it has a `compoundID`, and every
 * other field comes from the compound registry — or unknown, carrying at most a `formula` and a
 * user-entered `molWeight`. `molWeight` and `exactMass` are `NON_EMPTY`, so absent when blank.
 * `stereoisomerCode` and `saltCode` are always set, to the dictionaries' default items ("Achiral",
 * "00 - Parent Structure") unless chosen otherwise; `saltEQ` is absent for the parent structure.
 */
export interface CompoundRef {
  compoundID?: UUID;
  stereoisomerCode: DictionaryItemRef;
  saltCode: DictionaryItemRef;
  saltEQ?: number;
  /** The key in the compound's source system: an STR code for SRS, a CID for PubChem. */
  compoundKey?: string;
  formula?: MolFormula;
  molWeight?: EnteredValue;
  exactMass?: EnteredValue;
  casNumber?: string;
  /** Read-only, derived: HTML, the formula plus the salt. Absent without a `formula`. */
  calculatedBatchMF?: string;
}

/** Mirrors `CompoundRef.isKnown()`. Only an unknown compound takes a user-entered mol weight. */
export function isKnownCompound(compound: CompoundRef): compound is CompoundRef & { compoundID: UUID } {
  return compound.compoundID != null;
}

/* ── Enums ─────────────────────────────────────────────────────────────────────────────── */

/**
 * Where a reaction sample, or a catalog hit, comes from. `VIRTUAL` is an input sample with no
 * real sample behind it, or an output batch that is not registered yet.
 */
export type SampleSource = 'VIRTUAL' | 'SRS' | 'PUBCHEM';

export type ReactionRole = 'REACTANT' | 'REAGENT' | 'CATALYST' | 'SOLVENT' | 'OUTPUT';
export type ReactionOutputType = 'FINAL' | 'BY_PRODUCT' | 'INTERMEDIATE';
export type SampleRegistrationStatus = 'IN_PROGRESS' | 'FAILED' | 'REGISTERED';
export type ComparisonOperator = 'GREATER_THAN' | 'LESS_THAN' | 'EQUALS' | 'APPROXIMATELY';
export type PurityCalculationType = 'NMR' | 'HPLC' | 'LCMS' | 'CHN' | 'MS';
export type SolubidityType = 'QUANTITATIVE' | 'QUALITATIVE';
export type SolubidityQualitativeType = 'SOLUBLE' | 'UNSOLUBLE' | 'PRECIPITATE';

/* ── Enum companion tables ─────────────────────────────────────────────────────────────── */

export const REACTION_ROLES: readonly ReactionRole[] = ['REACTANT', 'REAGENT', 'CATALYST', 'SOLVENT', 'OUTPUT'];

export const REACTION_ROLE_LABELS: Record<ReactionRole, string> = {
  REACTANT: 'Reactant',
  REAGENT: 'Reagent',
  CATALYST: 'Catalyst',
  SOLVENT: 'Solvent',
  OUTPUT: 'Output',
};

/** The reaction roles an *input* row may take. `OUTPUT` is in the enum but never a choice here. */
export const INPUT_ROLES: readonly ReactionRole[] = ['REACTANT', 'REAGENT', 'CATALYST', 'SOLVENT'];

/** The three `ReactionOutputType` members, in the order the picker offers them. */
export const OUTPUT_TYPES: readonly ReactionOutputType[] = ['FINAL', 'BY_PRODUCT', 'INTERMEDIATE'];

/**
 * How each product type reads: the product the chemist was after, something the reaction threw
 * off along the way, or an intermediate the next step consumes. Shared, because the products
 * table offers the three in a picker and the batches table shows the owning product's type as a
 * read-only badge.
 *
 * **`FINAL` reads "Final", deliberately not "Intended"** — even though the design says the
 * latter. `ReactionOutput.intended` is a separate boolean saying the product was drawn in the
 * reaction scheme, and it is what decides membership of this table at all; every row here is
 * `intended` and any of the three types is reachable on it. Two adjacent concepts sharing one
 * word made the column unreadable.
 */
export const OUTPUT_TYPE_LABELS: Record<ReactionOutputType, string> = {
  FINAL: 'Final',
  BY_PRODUCT: 'Side',
  INTERMEDIATE: 'Intermediate',
};

/**
 * The colour each type carries. Read by `OutputTypeCell`'s select trigger in the products table
 * and by `OutputTypeBadge` in the batches one.
 */
export const OUTPUT_TYPE_TRIGGER_CLASS: Record<ReactionOutputType, string> = {
  FINAL: 'border-green-200 bg-green-10',
  BY_PRODUCT: 'border-orange-200 bg-orange-10',
  INTERMEDIATE: 'border-violet-200 bg-violet-10',
};

/**
 * How each registration state reads. A batch that has never been sent reads "None" rather than
 * being blank: an empty cell in this column would be indistinguishable from a missing value.
 */
export const REGISTRATION_STATUS_LABELS: Record<SampleRegistrationStatus, string> = {
  IN_PROGRESS: 'In Progress',
  FAILED: 'Failed',
  REGISTERED: 'Registered',
};

export const COMPARISON_OPERATORS: readonly ComparisonOperator[] = [
  'GREATER_THAN',
  'LESS_THAN',
  'EQUALS',
  'APPROXIMATELY',
];

/** `>`, `<`, `=`, `≈` — how each comparison reads in front of a number. */
export const OPERATOR_SYMBOLS: Record<ComparisonOperator, string> = {
  GREATER_THAN: '>',
  LESS_THAN: '<',
  EQUALS: '=',
  APPROXIMATELY: '≈',
};

export const SOLUBIDITY_TYPES: readonly SolubidityType[] = ['QUANTITATIVE', 'QUALITATIVE'];

export const SOLUBIDITY_TYPE_LABELS: Record<SolubidityType, string> = {
  QUANTITATIVE: 'Quantitative',
  QUALITATIVE: 'Qualitative',
};

export const SOLUBIDITY_QUALITATIVE_TYPES: readonly SolubidityQualitativeType[] = [
  'SOLUBLE',
  'UNSOLUBLE',
  'PRECIPITATE',
];

export const QUALITATIVE_LABELS: Record<SolubidityQualitativeType, string> = {
  SOLUBLE: 'Soluble',
  UNSOLUBLE: 'Insoluble',
  PRECIPITATE: 'Precipitate',
};

/* ── Output-sample value objects ───────────────────────────────────────────────────────── */

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

export interface ResidualSolvent {
  solvent: DictionaryItemRef;
  eq: number;
  comment?: string;
}

/**
 * Polymorphic on `type`, like `CompoundRef`: the discriminator on the wire is `type`, and
 * the two cases carry different fields.
 */
export type SolubidityInSolvent = { solvent: DictionaryItemRef; comment?: string } & (
  | { type: 'QUANTITATIVE'; operator?: ComparisonOperator; value?: number; unit?: DensityUnit }
  | { type: 'QUALITATIVE'; qualitativeType?: SolubidityQualitativeType }
);

/* ── Rows and samples ──────────────────────────────────────────────────────────────────── */

/** `ReactionRow` — what an input row and an output row share. */
interface ReactionRow {
  compound: CompoundRef;
  eq: EnteredValue;
  /** Position in the rxnfile; present only for roles that appear in the drawn scheme. */
  rxnPosition?: number;
}

/** `ReactionSample` — what an input sample and an output sample share. */
interface ReactionSample {
  density?: EnteredValue;
  molarity?: EnteredValue;
  volume?: EnteredValue;
  purity: EnteredValue;
  sampleSource: SampleSource;
  /** The key in `sampleSource`'s system — an STR code for SRS, a CID for PubChem. None for `VIRTUAL`. */
  sampleKey?: string;
  /** A set server-side: no duplicates, no meaningful order. */
  healthHazards: DictionaryItemRef[];
}

export interface ReactionInputSample extends ReactionSample {
  anchor: Anchor;
  nbkBatchNumber?: NbkBatchNumber;
  mol?: EnteredValue;
  weight?: EnteredValue;
  comment?: string;
}

export interface ReactionOutputSample extends ReactionSample {
  anchor: Anchor;
  nbkBatchNumber: NbkBatchNumber;
  actualMol?: EnteredValue;
  actualWeight?: EnteredValue;
  /** Java field `yieldValue`, remapped to `yield` by `@JsonProperty`. */
  yield?: EnteredValue;
  registrationStatus?: SampleRegistrationStatus;
  registrationStatusMessage?: string;
  handlingPrecautions: DictionaryItemRef[];
  storageInstructions: DictionaryItemRef[];
  compoundProtection: DictionaryItemRef[];
  solubilityInSolvents: SolubidityInSolvent[];
  residualSolvents: ResidualSolvent[];
  meltingPoint?: MeltingPoint;
  purityCalculations: PurityCalculation[];
  externalSupplier?: ExternalSupplier;
  source?: DictionaryItemRef;
  sourceDetails?: DictionaryItemRef;
  componentState?: DictionaryItemRef;
  batchComment?: string;
  structureComment?: string;
}

export interface ReactionInput extends ReactionRow {
  anchor: Anchor;
  role: ReactionRole;
  mol?: EnteredValue;
  /** Calculated, read-only: the sum of the samples' weights. */
  weight?: EnteredValue;
  /** Calculated, read-only: the sum of the samples' volumes. */
  volume?: EnteredValue;
  chemicalName?: string;
  samples: ReactionInputSample[];
  /**
   * Read-only, derived from the reaction's `limitingAnchor`, and `NON_DEFAULT` — so it is
   * absent rather than `false` on every row but the limiting one.
   */
  limiting?: boolean;
}

export interface ReactionOutput extends ReactionRow {
  anchor: Anchor;
  outputName: string;
  chemicalName?: string;
  type: ReactionOutputType;
  intended: boolean;
  theoMol?: EnteredValue;
  theoWeight?: EnteredValue;
  samples: ReactionOutputSample[];
}

/**
 * One step of an experiment. The `model`/`reaction`/`row` parents are `@JsonBackReference`
 * on the backend and are not on the wire, so the tree here is strictly downward.
 */
export interface Reaction {
  anchor: Anchor;
  /** Lowercase `f` — the `SetScheme` mutation spells the same thing `rxnFile`. */
  rxnfile?: string;
  inputs: ReactionInput[];
  /** Which input row is limiting; `ReactionInput.limiting` is derived from it. */
  limitingAnchor?: Anchor;
  outputs: ReactionOutput[];
}

/**
 * Mirrors `ExperimentModel`, the JSON blob in `experiment.model`. `reactions` are the steps,
 * and it is `@NotEmpty` — an experiment always has at least one.
 */
export interface ExperimentModel {
  reactions: Reaction[];
  significantFigures: number;
  unitSource: UnitSource;
}

/** Mirrors `UnitSource`: the unit every value of each quantity is in, experiment-wide. */
export interface UnitSource {
  weightUnit: WeightUnit;
  volumeUnit: VolumeUnit;
  molUnit: MolUnit;
  molarityUnit: MolarityUnit;
}
