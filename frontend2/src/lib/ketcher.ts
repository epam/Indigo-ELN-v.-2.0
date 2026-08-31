import type { StructService } from 'ketcher-core';

/**
 * The one place the app talks to Ketcher outside the editor component itself.
 * Storybook aliases this module to a stub (.storybook/main.ts) so no story drags
 * the 21 MB WASM bundle into the browser test run.
 *
 * Most of what follows works around two defects in `ketcher-standalone@3.17.2`'s
 * `IndigoService`, both rooted in `var indigoWorker = new WorkerFactory()` — one worker,
 * shared by every service ever constructed:
 *
 * 1. **A response resolves at most one pending call, but consumes them all.**
 *    `generateImageAsBase64` does `EE.once(GenerateImageAsBase64, action)` and `action`
 *    resolves only `if (msg.inputData === inputData)`. `once` removes *every* listener on
 *    the first response, so of two overlapping renders one resolves and the other is
 *    dropped with its listener already gone — its promise then never settles at all.
 *    `renderStructure` is therefore serialised: only one call is ever outstanding.
 *
 * 2. **The newest service steals the channel.** The constructor does
 *    `this.worker.onmessage = …` — an assignment, not `addEventListener` — so the last
 *    `IndigoService` built is the only one whose emitter still hears anything. Mounting
 *    the sketcher builds one (`ketcher-editor.tsx` has its own provider) and unmounting it
 *    restores nothing, which would leave this module permanently deaf after the first time
 *    a user opens the editor. So each call reinstalls our own handler and hands the channel
 *    back afterwards.
 *
 * Both are invisible in Storybook, where this module is stubbed. Recheck on every Ketcher
 * upgrade — a fixed upstream makes all of this dead weight.
 */

/** Enough of `IndigoService` to reach the worker it shares; not in the public typings. */
interface WorkerHolder {
  worker: { onmessage: unknown };
}

let servicePromise: Promise<StructService> | undefined;

/** The handler our own service installed, captured before anything else can overwrite it. */
let ownHandler: unknown;

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
 */
function structService(): Promise<StructService> {
  servicePromise ??= import('ketcher-standalone').then((module) => {
    const service = new module.StandaloneStructServiceProvider().createStructService({});
    (service as unknown as { getStandardServerOptions: () => object }).getStandardServerOptions = () => ({});
    ownHandler = (service as unknown as WorkerHolder).worker.onmessage;
    return service;
  });
  return servicePromise;
}

/**
 * Long enough that a cold WASM compile (~1.3 s) is never cut short, short enough that a
 * response which is never coming stops holding a spinner up. Reaching it means something
 * ate our reply — a worker blocked by CSP, or a service built while ours was mid-call.
 */
const RENDER_TIMEOUT_MS = 30_000;

let queue: Promise<unknown> = Promise.resolve();

/** Runs `task` once every earlier one has settled, successfully or not. */
function enqueue<T>(task: () => Promise<T>): Promise<T> {
  const result = queue.then(task, task);
  queue = result.catch(() => undefined);
  return result;
}

async function generateSvg(structure: string): Promise<string> {
  const service = await structService();
  const worker = (service as unknown as WorkerHolder).worker;

  // Borrowed, not taken: an editor mounted right now is mid-conversation on this same
  // channel, and gets it back below. Ours is usually already installed, making both a no-op.
  const borrowedFrom = worker.onmessage;
  worker.onmessage = ownHandler;

  let timer: ReturnType<typeof setTimeout> | undefined;
  try {
    const base64 = await Promise.race([
      service.generateImageAsBase64(structure, { outputFormat: 'svg' }),
      new Promise<never>((_resolve, reject) => {
        timer = setTimeout(() => {
          // The abandoned `once` listener is still on this service's emitter and would eat
          // the next response, so the service is dropped and the next call builds a fresh
          // one. The dynamic import is already cached, so that costs nothing.
          servicePromise = undefined;
          ownHandler = undefined;
          reject(new Error('Rendering the structure timed out'));
        }, RENDER_TIMEOUT_MS);
      }),
    ]);
    // Indigo answers with bare base64, not a data URL.
    return `data:image/svg+xml;base64,${base64}`;
  } finally {
    clearTimeout(timer);
    worker.onmessage = borrowedFrom;
  }
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
 *
 * It shares `renderStructure`'s queue rather than racing it. Revisiting an experiment is
 * exactly when they collide: the loader runs this again while the cached query lets
 * `SchemeEditor` mount in the same tick, and before the queue existed the throwaway render
 * would swallow the real one's response and hang it forever.
 */
export function prewarmKetcher(): void {
  void enqueue(() => generateSvg('C')).catch(() => {});
}

/**
 * Renders a molfile or rxnfile to an SVG `data:` URL.
 *
 * Deliberately not memoised. `ketcher-standalone` holds its Indigo worker as a module
 * singleton, which every struct service — this one and the editor's alike — shares, so
 * once anything has rendered on the page the WASM is compiled and a call costs 4–8 ms.
 * Caching that bought nothing but an unbounded Map keyed by whole molfiles, and an object
 * URL nothing could know when to revoke. The result is a plain string with no lifetime of
 * its own; whoever displays it owns it, and a structure that comes back into view is
 * simply rendered again.
 */
export function renderStructure(structure: string): Promise<string> {
  return enqueue(() => generateSvg(structure));
}
