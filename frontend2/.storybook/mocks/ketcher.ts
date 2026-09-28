/**
 * Stands in for src/lib/ketcher.ts. The real module pulls ~28 MB of sketcher and Indigo
 * WASM, which no story should pay for — and which the a11y run in headless Chromium
 * would time out on.
 */

const BENZENE_SVG =
  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><polygon points="50,8 86,29 86,71 50,92 14,71 14,29" fill="none" stroke="black" stroke-width="3"/><circle cx="50" cy="50" r="22" fill="none" stroke="black" stroke-width="3"/></svg>';

export const MOCK_STRUCTURE_IMAGE = `data:image/svg+xml;base64,${btoa(BENZENE_SVG)}`;

export function prewarmKetcher(): void {}

export function renderStructure(): Promise<string> {
  return Promise.resolve(MOCK_STRUCTURE_IMAGE);
}
