import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

/**
 * A miniature `ketcher-standalone` that reproduces the two `IndigoService` quirks
 * `src/lib/ketcher.ts` exists to survive — see the comment at the top of that file.
 *
 * Everything here is a claim about the vendored package, so if a Ketcher upgrade changes
 * this behaviour, this fake is what needs re-checking against `dist/main.js`.
 */
const fake = vi.hoisted(() => {
  interface Response {
    inputData: string;
    hasError?: boolean;
    payload?: string;
  }

  /** `var indigoWorker = new WorkerFactory()` — one worker, shared by every service. */
  const worker = {
    onmessage: null as ((event: { data: Response }) => void) | null,
    /** Set to stop the worker answering at all, standing in for a CSP-blocked one. */
    silent: false,
    postMessage(message: { data: { struct: string } }) {
      if (worker.silent) return;
      const { struct } = message.data;
      setTimeout(() => worker.onmessage?.({ data: { inputData: struct, payload: `svg(${struct})` } }), 0);
    },
  };

  class FakeIndigoService {
    worker = worker;
    private listeners: ((message: Response) => void)[] = [];

    constructor() {
      // Quirk 2: a plain assignment, so the newest service silences every earlier one.
      worker.onmessage = (event) => {
        // Quirk 1: `EE.once` drops *every* listener on the first response, matching or not.
        const fired = this.listeners;
        this.listeners = [];
        for (const listener of fired) listener(event.data);
      };
    }

    getStandardServerOptions() {
      return {};
    }

    generateImageAsBase64(inputData: string) {
      return new Promise<string>((resolve, reject) => {
        this.listeners.push((message) => {
          // Quirk 1 again: a response for someone else is silently ignored, and by then this
          // listener has already been removed — so that call can never settle.
          if (message.inputData !== inputData) return;
          if (message.hasError) reject(new Error('render failed'));
          else resolve(message.payload!);
        });
        worker.postMessage({ data: { struct: inputData } });
      });
    }
  }

  return {
    worker,
    newService: () => new FakeIndigoService(),
    module: {
      StandaloneStructServiceProvider: class {
        createStructService() {
          return new FakeIndigoService();
        }
      },
    },
  };
});

vi.mock('ketcher-standalone', () => fake.module);

/** Fresh module state per test: the queue and the memoised service are module-level. */
async function loadKetcher() {
  vi.resetModules();
  return import('@/lib/ketcher');
}

beforeEach(() => {
  fake.worker.silent = false;
  fake.worker.onmessage = null;
});

afterEach(() => {
  vi.useRealTimers();
});

describe('renderStructure', () => {
  it('returns a data URL around Indigo’s bare base64', async () => {
    const { renderStructure } = await loadKetcher();
    await expect(renderStructure('CCO')).resolves.toBe('data:image/svg+xml;base64,svg(CCO)');
  });

  /**
   * The reported bug: revisiting an experiment runs the route loader's prewarm again while the
   * cached query lets `SchemeEditor` mount in the same tick. Unserialised, the throwaway render
   * consumes the real one's listener and the scheme shows its skeleton forever.
   */
  it('survives a prewarm fired in the same tick', async () => {
    const { prewarmKetcher, renderStructure } = await loadKetcher();

    prewarmKetcher();
    await expect(renderStructure('RXN')).resolves.toBe('data:image/svg+xml;base64,svg(RXN)');
  });

  it('serialises overlapping renders rather than losing one', async () => {
    const { renderStructure } = await loadKetcher();

    await expect(Promise.all([renderStructure('A'), renderStructure('B')])).resolves.toEqual([
      'data:image/svg+xml;base64,svg(A)',
      'data:image/svg+xml;base64,svg(B)',
    ]);
  });

  /** Opening the sketcher builds a second service, which takes the worker's only handler. */
  it('renders again after the editor has taken the message channel', async () => {
    const { renderStructure } = await loadKetcher();
    await renderStructure('first');

    const editorService = fake.newService();
    expect(fake.worker.onmessage).not.toBeNull();

    await expect(renderStructure('second')).resolves.toBe('data:image/svg+xml;base64,svg(second)');
    // Handed back, so an editor that is still mounted keeps working.
    await expect(editorService.generateImageAsBase64('editor')).resolves.toBe('svg(editor)');
  });

  /** A response that never comes must reject, not hold the frame's skeleton up forever. */
  it('rejects instead of hanging when nothing answers', async () => {
    vi.useFakeTimers();
    const { renderStructure } = await loadKetcher();
    fake.worker.silent = true;

    const pending = renderStructure('never');
    const assertion = expect(pending).rejects.toThrow('timed out');
    await vi.advanceTimersByTimeAsync(30_000);
    await assertion;
  });

  it('recovers on the next call after a timeout', async () => {
    vi.useFakeTimers();
    const { renderStructure } = await loadKetcher();

    fake.worker.silent = true;
    const timedOut = expect(renderStructure('never')).rejects.toThrow('timed out');
    await vi.advanceTimersByTimeAsync(30_000);
    await timedOut;

    // Back to real timers: the fake worker answers through a `setTimeout`, so the recovery
    // render would never get its reply on the fake clock.
    vi.useRealTimers();
    fake.worker.silent = false;
    await expect(renderStructure('after')).resolves.toBe('data:image/svg+xml;base64,svg(after)');
  });
});
