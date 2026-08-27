import type { StructService } from 'ketcher-core';

/**
 * The one place the app talks to Ketcher outside the editor component itself.
 * Storybook aliases this module to a stub (.storybook/main.ts) so no story drags
 * the 21 MB WASM bundle into the browser test run.
 */

let servicePromise: Promise<StructService> | undefined;

/**
 * The standalone (WASM) struct service, built once and lazily: ketcher-standalone and
 * the Indigo bundle inside it are ~21 MB, so it must never land in the initial chunk.
 *
 * Ketcher 3.17 offers no headless rendering path — every struct-service call resolves
 * its Indigo defaults through `getStandardServerOptions`, which looks the *calling
 * editor* up in the global `ketcherProvider` and reads render settings off it. This
 * service instance is ours alone and drives no editor, so we short-circuit that lookup
 * to Indigo's own defaults. Registering a fake instance in the provider instead would
 * be worse: `getKetcher(undefined)` hands out the most recently registered instance,
 * so a stub sitting in there breaks a real <Editor> as it mounts.
 *
 * Recheck on every Ketcher upgrade.
 */
function structService(): Promise<StructService> {
  servicePromise ??= import('ketcher-standalone').then((module) => {
    const service = new module.StandaloneStructServiceProvider().createStructService({});
    (service as unknown as { getStandardServerOptions: () => object }).getStandardServerOptions = () => ({});
    return service;
  });
  return servicePromise;
}

/**
 * Starts the whole cold path early — module fetch, worker spawn, WASM compile — so a
 * screen that is about to show a structure does not pay for it at the moment it draws.
 * The throwaway render matters: fetching the module is most of the cost, but Indigo
 * does not compile until something is actually rendered.
 *
 * Fire and forget. Failures are swallowed on purpose — this is an optimisation, and
 * the real call reports the problem if there turns out to be one. Note that it is
 * called from a route loader, so turning on `defaultPreload` in `main.tsx` would start
 * pulling ~21 MB on link hover.
 */
export function prewarmKetcher(): void {
  void structService()
    .then((service) => service.generateImageAsBase64('C', { outputFormat: 'svg' }))
    .catch(() => {});
}

/**
 * Renders a molfile or rxnfile to an SVG `data:` URL.
 *
 * Deliberately not memoised. `ketcher-standalone` holds its Indigo worker as a module
 * singleton (`var indigoWorker = new WorkerFactory()`), which every struct service —
 * this one and the editor's alike — shares, so once anything has rendered on the page
 * the WASM is compiled and a call costs 4–8 ms. Caching that bought nothing but an
 * unbounded Map keyed by whole molfiles, and an object URL nothing could know when to
 * revoke. The result is a plain string with no lifetime of its own; whoever displays it
 * owns it, and a structure that comes back into view is simply rendered again.
 */
export async function renderStructure(structure: string): Promise<string> {
  const service = await structService();
  // Indigo answers with bare base64, not a data URL.
  const base64 = await service.generateImageAsBase64(structure, { outputFormat: 'svg' });
  return `data:image/svg+xml;base64,${base64}`;
}
