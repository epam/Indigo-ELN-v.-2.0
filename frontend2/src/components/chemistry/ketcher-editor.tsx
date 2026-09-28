import { Editor } from 'ketcher-react';
import { StandaloneStructServiceProvider } from 'ketcher-standalone';

import 'ketcher-react/dist/index.css';

import { notifyError } from '@/lib/toast';

import type { Ketcher } from 'ketcher-core';

/**
 * The Ketcher sketcher itself. This module — and nothing else — imports ketcher-react,
 * so the ~28 MB behind it stays in one chunk that is only fetched when someone actually
 * draws something. Always reach it through `lazy(() => import(...))`.
 */

// One provider for the whole app: the Indigo WASM worker inside it is a singleton anyway.
const structServiceProvider = new StandaloneStructServiceProvider();

interface KetcherEditorProps {
  /** Called once the sketcher is live and its instance can be driven. */
  onReady: (ketcher: Ketcher) => void;
}

function KetcherEditor({ onReady }: KetcherEditorProps) {
  return (
    <Editor
      staticResourcesUrl=""
      structServiceProvider={structServiceProvider}
      errorHandler={(message: string) => notifyError(new Error(String(message)))}
      onInit={onReady}
    />
  );
}

export default KetcherEditor;
