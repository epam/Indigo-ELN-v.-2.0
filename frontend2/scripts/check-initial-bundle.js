/**
 * Fails the build if anything oversized lands in the initial module graph.
 *
 * The invariant that matters is Ketcher's: ketcher-standalone plus the Indigo WASM inlined
 * into it is a 21 MB chunk, and it must only ever be reached through a dynamic `import()`.
 * A stray static import somewhere would pull it into the entry graph and nothing else in
 * the pipeline would notice — the build succeeds, the tests pass, and the app just becomes
 * unusable on a cold load.
 *
 * It is deliberately a size budget rather than a check for the name "ketcher": the same
 * mistake with any other heavy dependency is just as bad, and chunk names are generated.
 * Note that a *small* ketcher chunk in the preload list is fine and expected — the module
 * holding `prewarmKetcher` is ~700 bytes, and the 21 MB stays behind its `import()`.
 *
 * Budgets are generous on purpose; they catch a category error, not gradual growth.
 * At the time of writing: 527 KB total, 178 KB for the largest single chunk (react-dom).
 */
import {readFileSync, statSync} from 'node:fs';
import {join} from 'node:path';

const DIST = 'dist';
const MAX_CHUNK_BYTES = 1_000_000;
const MAX_TOTAL_BYTES = 1_500_000;

const html = (() => {
  try {
    return readFileSync(join(DIST, 'index.html'), 'utf8');
  } catch {
    console.error(`No ${DIST}/index.html — run \`pnpm run build\` first.`);
    process.exit(1);
  }
})();

// The entry script plus everything the browser is told to preload alongside it: together,
// the JavaScript a cold page load pays for before it can render.
// The leading [^"]* absorbs Vite's `base` (/frontend2/), leaving the captured path relative
// to dist/ either way.
const paths = [...html.matchAll(/(?:src|href)="[^"]*\/(assets\/[^"]+\.js)"/g)].map((match) => match[1]);

if (paths.length === 0) {
  console.error(`Found no entry or preloaded scripts in ${DIST}/index.html — has the build layout changed?`);
  process.exit(1);
}

const chunks = paths
  .map((path) => ({ path, bytes: statSync(join(DIST, path)).size }))
  .sort((a, b) => b.bytes - a.bytes);

const total = chunks.reduce((sum, chunk) => sum + chunk.bytes, 0);
const kb = (bytes) => `${(bytes / 1024).toFixed(1)} kB`;

const oversized = chunks.filter((chunk) => chunk.bytes > MAX_CHUNK_BYTES);
const overBudget = total > MAX_TOTAL_BYTES;

if (oversized.length > 0 || overBudget) {
  console.error(`Initial bundle over budget — ${chunks.length} chunks, ${kb(total)} total:\n`);
  for (const chunk of chunks) {
    console.error(`  ${chunk.bytes > MAX_CHUNK_BYTES ? '✗' : ' '} ${kb(chunk.bytes).padStart(10)}  ${chunk.path}`);
  }
  console.error('');
  for (const chunk of oversized) {
    console.error(`✗ ${chunk.path} is ${kb(chunk.bytes)}, over the ${kb(MAX_CHUNK_BYTES)} per-chunk budget.`);
  }
  if (overBudget) console.error(`✗ ${kb(total)} total, over the ${kb(MAX_TOTAL_BYTES)} budget.`);
  console.error('\nUsually this means something heavy gained a static import and left its dynamic import().');
  process.exit(1);
}

console.log(
  `Initial bundle: ${chunks.length} chunks, ${kb(total)} total, largest ${kb(chunks[0].bytes)}. Within budget.`,
);
