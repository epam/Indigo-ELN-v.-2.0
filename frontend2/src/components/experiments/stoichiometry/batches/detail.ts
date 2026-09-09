import type {
  ExternalSupplier,
  MeltingPoint,
  PurityCalculation,
  ResidualSolvent,
  SolubidityInSolvent,
} from '@/lib/types/reactions.ts';
import { OPERATOR_SYMBOLS, QUALITATIVE_LABELS, unitLabel } from '@/lib/types/reactions.ts';

/**
 * How the batch detail panel writes the five composite value-objects it shows but cannot yet
 * edit — melting point, residual solvents, solubility, external supplier and purity
 * calculations.
 *
 * They live here rather than in the panel so the component file exports only components and Fast
 * Refresh keeps working, the same split `project-form.ts` makes.
 *
 * Every one of these returns `undefined` (or an empty list) for "nothing to show" rather than an
 * em-dash: the read-only field renders the dash itself, so a formatter that baked one in would
 * make an absent value indistinguishable from a value that formats to a dash.
 *
 * They are deliberately richer than indigo-frontend's, which prints the solvent names alone and
 * drops the equivalents, the operators and the qualitative type — the numbers are the point of
 * the record, and this is the only place they are shown at all.
 */

/**
 * `67 ~ 69 °C`, or a single bound on its own when only one was recorded.
 *
 * `MeltingPoint` has three optional members, so an object with nothing but a comment on it is a
 * value the wire can produce — and one with no temperature in it has nothing to say here.
 */
export function meltingPointLabel(meltingPoint: MeltingPoint | undefined): string | undefined {
  if (meltingPoint == null) return undefined;

  const { lower, upper, comments } = meltingPoint;
  const range =
    lower != null && upper != null
      ? `${lower} ~ ${upper} °C`
      : lower != null
        ? `${lower} °C`
        : upper != null
          ? `${upper} °C`
          : undefined;

  if (range == null) return comments || undefined;
  return comments ? `${range} (${comments})` : range;
}

/** One chip per solvent: `Toluene (1.2 eq)`. */
export function residualSolventLabels(solvents: ResidualSolvent[] | undefined): string[] {
  return (solvents ?? []).map((solvent) => `${solvent.solvent.name} (${solvent.eq} eq)`);
}

/**
 * One chip per solvent, branching on the `type` discriminator: a quantitative entry reads
 * `Water > 5 g/mL`, a qualitative one `Water: Insoluble`.
 *
 * Both cases carry optional members — a `QUANTITATIVE` entry with no value yet, a `QUALITATIVE`
 * one with no verdict — so either can fall back to the solvent's name alone.
 */
export function solubilityLabels(solvents: SolubidityInSolvent[] | undefined): string[] {
  return (solvents ?? []).map((entry) => {
    const name = entry.solvent.name;

    if (entry.type === 'QUANTITATIVE') {
      if (entry.value == null) return name;
      const operator = entry.operator ? `${OPERATOR_SYMBOLS[entry.operator]} ` : '';
      const unit = unitLabel(entry.unit);
      return `${name} ${operator}${entry.value}${unit && ` ${unit}`}`;
    }

    return entry.qualitativeType ? `${name}: ${QUALITATIVE_LABELS[entry.qualitativeType]}` : name;
  });
}

/** `Sigma-Aldrich (A1234)`, or the supplier alone when no registry number was given. */
export function externalSupplierLabel(supplier: ExternalSupplier | undefined): string | undefined {
  if (supplier == null) return undefined;
  return supplier.registryNumber ? `${supplier.supplier.name} (${supplier.registryNumber})` : supplier.supplier.name;
}

/** One chip per measurement: `HPLC > 98`. */
export function purityCalculationLabels(calculations: PurityCalculation[] | undefined): string[] {
  return (calculations ?? []).map(
    (calculation) => `${calculation.type} ${OPERATOR_SYMBOLS[calculation.operator]} ${calculation.purity}`,
  );
}
