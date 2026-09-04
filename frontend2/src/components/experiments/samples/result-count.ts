/**
 * What a tab's `(…)` says about how many hits its search found.
 *
 * `/samples/search` cannot always answer that. It walks several catalogs and sums their counts,
 * but PubChem reports none — `SampleSearchService.plus` returns null as soon as one of them
 * does — so a search that reached PubChem comes back with `totalItems: null` however many rows
 * it carried. The count is then only knowable from below: at least as many as have loaded.
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
  /** `SampleSearchResult.totalItems`; null when a catalog that cannot count contributed. */
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
