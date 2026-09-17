import { useEffect } from 'react';

import type { Ketcher } from 'ketcher-core';

/** Stands in for the real ketcher-react editor; see ./ketcher.ts for why. */

const MOLFILE = 'mock-molfile';

let isReaction = false;
let atomCount = 1;

const fakeKetcher = {
  setMolecule: () => Promise.resolve(),
  containsReaction: () => isReaction,
  getMolfile: () => Promise.resolve(MOLFILE),
  getRxn: () => Promise.resolve(MOLFILE),
  // What `StructureEditorDialog` reads to refuse a canvas with nothing on it.
  editor: { struct: () => ({ atoms: { size: atomCount } }) },
} as unknown as Ketcher;

/**
 * Chooses what the next Save reads off the canvas — a reaction, or a drawing with no atoms. A
 * story sets it in `beforeEach`; passing nothing puts both back to an ordinary molecule, so one
 * story cannot leak its canvas into the next.
 */
export function __setKetcherBehavior(next?: { isReaction?: boolean; atomCount?: number }) {
  isReaction = next?.isReaction ?? false;
  atomCount = next?.atomCount ?? 1;
}

function KetcherEditor({ onReady }: { onReady: (ketcher: Ketcher) => void }) {
  useEffect(() => {
    onReady(fakeKetcher);
    // Mounting once is the whole point; onReady is a fresh closure on every render.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return <div className="flex size-full items-center justify-center bg-neutral-200">Ketcher (mocked)</div>;
}

export default KetcherEditor;
