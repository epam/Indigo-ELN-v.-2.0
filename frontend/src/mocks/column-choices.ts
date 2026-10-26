/**
 * Forgets every Column Selector choice in this browser. The choice lives in localStorage, and all
 * stories share one browser, so a story that changes it clears it before and after itself.
 */
export function clearColumnChoices() {
  for (const key of Object.keys(localStorage)) {
    if (key.endsWith('.hiddenColumns')) localStorage.removeItem(key);
  }
}
