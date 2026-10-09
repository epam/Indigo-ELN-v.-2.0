/**
 * What the sample-search result table needs to know that is not a query: how a count is worded,
 * and what identifies a row. See `src/lib/api/samples.ts` for the paging itself.
 */

import type { SampleSource } from '@/lib/types/reactions.ts';
import type { SampleDTO } from '@/lib/types/samples.ts';

/**
 * What a tab's `(…)` says about how many hits its search found.
 *
 * `/samples/search` cannot always answer that: PubChem reports no count, so its page comes back
 * with `totalItems: null` however many rows it carried. The count is then only knowable from
 * below: at least as many as have loaded.
 *
 * Three cases, and the middle one is the one worth stating:
 *
 * - The server counted, so say the count.
 * - It did not, but there is no next page — the search is exhausted, so what has loaded *is*
 *   the total, and a `+` would claim there is more when there is not.
 * - It did not and more remains: a lower bound, `12+`.
 *
 * `null` means show nothing at all, which is the honest answer before the first page lands.
 */
export function resultCountLabel({
  totalItems,
  loaded,
  hasMore,
  loading,
}: {
  /** The first page's `totalItems`; null when the catalog cannot count. */
  totalItems: number | null;
  /** How many rows are on screen, across every page fetched so far. */
  loaded: number;
  /** Whether the cursor points at another page. */
  hasMore: boolean;
  /** Whether the first page is still in flight. */
  loading: boolean;
}): string | null {
  if (loading) return null;
  if (totalItems != null) return String(totalItems);
  return hasMore ? `${loaded}+` : String(loaded);
}

/** The one identity a sample has across catalogs and the reaction model: its system and its key there. */
export function sampleKeyOf(source: SampleSource, sampleKey: string): string {
  return `${source}:${sampleKey}`;
}

/**
 * What identifies a result row — for its spinner, for the My Materials patch, and for whether
 * the step already holds it (`getAllInputSampleKeys`).
 *
 * Not the catalog: My Materials answers with the same SRS or PubChem sample the other tabs do,
 * and marking it in one has to show in both. Not array index either: a second page arriving
 * renumbers nothing, but a row moving between tabs would.
 */
export function sampleRowKey(sample: SampleDTO): string {
  return sampleKeyOf(sample.source, sample.sampleKey);
}
