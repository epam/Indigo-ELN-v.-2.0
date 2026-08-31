import {config, z} from 'zod';

/**
 * The app's single entry point to zod. Import `z` from here, never from 'zod' directly.
 *
 * zod probes `new Function('')` to decide whether to compile its object parsers. Under the
 * Content-Security-Policy the app is served with (see `vite.config.ts`), that probe throws;
 * zod catches it and falls back to the interpreted parser, so validation still works, but
 * the browser reports a `script-src` violation and logs an error on every load. `jitless`
 * skips the probe, which is what the CSP would force zod into anyway.
 *
 * The call has to happen before the first schema is built, and that is why this module
 * exists rather than a line in `main.tsx`: an ESM import is evaluated before the importing
 * module's body, so routing every schema through here puts the config first no matter which
 * chunk loads when. Configuring from `main.tsx` is too late — route chunks are evaluated
 * before the entry chunk's body runs.
 */
config({ jitless: true });

export { z };
