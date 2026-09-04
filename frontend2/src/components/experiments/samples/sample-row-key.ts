import type { SampleDTO } from '@/lib/types/samples.ts';

/**
 * What identifies a result row for the purpose of showing a spinner on it.
 *
 * A PubChem hit has no `id` — that is the whole reason `importFromSearch` exists — so it is
 * identified by the catalog that produced it plus that catalog's own key (the CID). Not by array
 * index: a second page arriving renumbers nothing, but a row moving between tabs would.
 */
export function sampleRowKey(sample: SampleDTO): string {
  return sample.id ?? `${sample.source}:${sample.compoundKey ?? sample.molFormula}`;
}
