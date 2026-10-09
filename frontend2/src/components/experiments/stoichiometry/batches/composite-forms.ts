import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type {
  ComparisonOperator,
  MeltingPoint,
  ResidualSolvent,
  SolubidityInSolvent,
  SolubidityQualitativeType,
  SolubidityType,
} from '@/lib/types/reactions.ts';

/**
 * What the three composite editors of the batch detail panel hold while open, and how that maps
 * to and from the value-objects the `SetOutput*` mutations take.
 *
 * Numbers are strings here, as a number box holds them, and an unpicked select is `null`. A list
 * editor always shows at least one row, so a row nobody touched is not an entry: it is dropped on
 * the way out rather than reported as incomplete.
 */

function toNumber(text: string): number | undefined {
  return text.trim() === '' ? undefined : Number(text);
}

function toText(text: string): string | undefined {
  return text.trim() || undefined;
}

function fromNumber(value: number | undefined): string {
  return value == null ? '' : String(value);
}

/** `Comment, row 2` — what a control under a column heading shared by every row is called. */
export function rowLabel(header: string, index: number): string {
  return `${header}, row ${index + 1}`;
}

/* ── Melting point ─────────────────────────────────────────────────────────────────────── */

export interface MeltingPointForm {
  lower: string;
  upper: string;
  comments: string;
}

export function toMeltingPointForm(meltingPoint: MeltingPoint | undefined): MeltingPointForm {
  return {
    lower: fromNumber(meltingPoint?.lower),
    upper: fromNumber(meltingPoint?.upper),
    comments: meltingPoint?.comments ?? '',
  };
}

/** `null` when nothing is filled in — `SetOutputMeltingPoint` clears the value with it. */
export function toMeltingPoint(form: MeltingPointForm): MeltingPoint | null {
  const lower = toNumber(form.lower);
  const upper = toNumber(form.upper);
  const comments = toText(form.comments);
  if (lower == null && upper == null && comments == null) return null;
  return { lower, upper, comments };
}

/* ── Residual solvents ─────────────────────────────────────────────────────────────────── */

export interface ResidualSolventRow {
  solvent: DictionaryItemRef | null;
  eq: string;
  comment: string;
}

export const EMPTY_RESIDUAL_SOLVENT_ROW: ResidualSolventRow = { solvent: null, eq: '', comment: '' };

function isBlankResidualSolventRow(row: ResidualSolventRow): boolean {
  return row.solvent == null && row.eq.trim() === '' && row.comment.trim() === '';
}

export function toResidualSolventRows(solvents: ResidualSolvent[]): ResidualSolventRow[] {
  if (solvents.length === 0) return [EMPTY_RESIDUAL_SOLVENT_ROW];
  return solvents.map((entry) => ({ solvent: entry.solvent, eq: String(entry.eq), comment: entry.comment ?? '' }));
}

/** `ResidualSolvent` is `@NotNull` on both the solvent and the EQ. */
export function areResidualSolventRowsValid(rows: ResidualSolventRow[]): boolean {
  return rows.every((row) => isBlankResidualSolventRow(row) || (row.solvent != null && row.eq.trim() !== ''));
}

/** Only for rows `areResidualSolventRowsValid` has passed. */
export function toResidualSolvents(rows: ResidualSolventRow[]): ResidualSolvent[] {
  return rows.flatMap((row) =>
    row.solvent == null ? [] : [{ solvent: row.solvent, eq: Number(row.eq), comment: toText(row.comment) }],
  );
}

/* ── Solubility in solvents ────────────────────────────────────────────────────────────── */

/**
 * Flat, where `SolubidityInSolvent` is a union: a row keeps both sides' fields so switching the
 * type and back does not lose what was typed. Only the current type's fields are sent.
 */
export interface SolubilityRow {
  solvent: DictionaryItemRef | null;
  type: SolubidityType;
  operator: ComparisonOperator;
  value: string;
  qualitativeType: SolubidityQualitativeType | null;
  comment: string;
}

export const EMPTY_SOLUBILITY_ROW: SolubilityRow = {
  solvent: null,
  type: 'QUANTITATIVE',
  operator: 'GREATER_THAN',
  value: '',
  qualitativeType: null,
  comment: '',
};

/** The type and the operator always hold something, so they do not make a row touched. */
function isBlankSolubilityRow(row: SolubilityRow): boolean {
  return row.solvent == null && row.value.trim() === '' && row.qualitativeType == null && row.comment.trim() === '';
}

export function toSolubilityRows(solvents: SolubidityInSolvent[]): SolubilityRow[] {
  if (solvents.length === 0) return [EMPTY_SOLUBILITY_ROW];
  return solvents.map((entry) => ({
    ...EMPTY_SOLUBILITY_ROW,
    solvent: entry.solvent,
    comment: entry.comment ?? '',
    ...(entry.type === 'QUANTITATIVE'
      ? { type: entry.type, operator: entry.operator ?? EMPTY_SOLUBILITY_ROW.operator, value: fromNumber(entry.value) }
      : { type: entry.type, qualitativeType: entry.qualitativeType ?? null }),
  }));
}

/** `SolubidityInSolvent` is `@NotNull` on the solvent alone. */
export function areSolubilityRowsValid(rows: SolubilityRow[]): boolean {
  return rows.every((row) => isBlankSolubilityRow(row) || row.solvent != null);
}

/**
 * Only for rows `areSolubilityRowsValid` has passed.
 *
 * An operator and a unit say nothing without a number, so the three travel together. The unit is
 * `G_ML` because `DensityUnit` has no other member.
 */
export function toSolubilityInSolvents(rows: SolubilityRow[]): SolubidityInSolvent[] {
  return rows.flatMap((row): SolubidityInSolvent[] => {
    if (row.solvent == null) return [];
    const base = { solvent: row.solvent, comment: toText(row.comment) };

    if (row.type === 'QUALITATIVE') {
      return [{ ...base, type: 'QUALITATIVE', qualitativeType: row.qualitativeType ?? undefined }];
    }
    const value = toNumber(row.value);
    return [
      value == null
        ? { ...base, type: 'QUANTITATIVE' }
        : { ...base, type: 'QUANTITATIVE', operator: row.operator, value, unit: 'G_ML' },
    ];
  });
}

/**
 * Whether an editor's Save would change anything. Both sides come out of the same `to*` function,
 * so their keys are in the same order and absent members are `undefined` on both.
 */
export function isSameValue<T>(a: T, b: T): boolean {
  return JSON.stringify(a) === JSON.stringify(b);
}
