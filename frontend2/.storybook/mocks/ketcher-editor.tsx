import { useEffect } from 'react';

import type { Ketcher } from 'ketcher-core';

/** Stands in for the real ketcher-react editor; see ./ketcher.ts for why. */

const MOLFILE = 'mock-molfile';

const fakeKetcher = {
  setMolecule: () => Promise.resolve(),
  containsReaction: () => false,
  getMolfile: () => Promise.resolve(MOLFILE),
  getRxn: () => Promise.resolve(MOLFILE),
} as unknown as Ketcher;

function KetcherEditor({ onReady }: { onReady: (ketcher: Ketcher) => void }) {
  useEffect(() => {
    onReady(fakeKetcher);
    // Mounting once is the whole point; onReady is a fresh closure on every render.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return <div className="flex size-full items-center justify-center bg-neutral-200">Ketcher (mocked)</div>;
}

export default KetcherEditor;
