import crypto from 'node:crypto';
import fs from 'node:fs';
import {availableParallelism} from 'node:os';
import {fileURLToPath} from 'node:url';
import {storybookTest} from '@storybook/addon-vitest/vitest-plugin';
import tailwindcss from '@tailwindcss/vite';
import {tanstackRouter} from '@tanstack/router-plugin/vite';
import react from '@vitejs/plugin-react';
import {playwright} from '@vitest/browser-playwright';
import {defineConfig} from 'vitest/config';

import type {PluginOption} from 'vite';

// The deployed app is served as static files from S3/CloudFront, where /api is
// same-origin (CloudFront forwards it to the API Gateway origin). Only the dev
// server needs a proxy.
//
// The target must be the CloudFront domain, not the API Gateway URL directly:
// CloudFront injects the X-API-Secret header the API origin expects
// (CloudFrontStack.java, apiBehavior).
const API_TARGET = 'https://indigo-eln-dev.test.lifescience.opensource.epam.com';

// Playwright's bundled Chromium needs a set of system libraries that only
// `playwright install --with-deps` (i.e. root) can put in place. On a machine that already
// has Chromium installed natively, CHROMIUM_BIN points the browser tests at that binary
// instead and no root is needed — see the `test:stories:native` script.
const CHROMIUM_BIN = process.env.CHROMIUM_BIN;

// Both projects must declare the same `maxWorkers`: Vitest 4 refuses to run two projects that
// share a `sequence.groupOrder` (both default to 0) with different worker counts. The number is
// the browser project's — see the measurement note on its `maxWorkers` below — and the unit
// project simply follows it, since 4 jsdom workers are not the bottleneck there.
const MAX_WORKERS = Math.min(4, availableParallelism());

// The deployed app is served behind a Content-Security-Policy. `vite preview` serves the
// real production bundle, so pointing the same policy at it is the only way to find a
// violation before CloudFront does — the dev server cannot be used for this, because its
// HMR client needs inline scripts the production policy forbids.
//
// The policy is hash-based rather than nonce-based: the build emits no inline <script> or
// <style> at all, so `'self'` already covers everything and the hash lists below come out
// empty. They exist so that adding an inline snippet later keeps working instead of
// silently needing a nonce (and, with it, a Lambda@Edge to mint one per request).
const CSP_HASHES_FILE = 'dist/csp-hashes.json';

function sha256(source: string): string {
  return `'sha256-${crypto.createHash('sha256').update(source, 'utf8').digest('base64')}'`;
}

/** Hashes every inline <script> and <style> the build put in index.html. */
function cspHashes(): PluginOption {
  return {
    name: 'csp-hashes',
    apply: 'build',
    closeBundle() {
      const html = fs.readFileSync('dist/index.html', 'utf8');
      const hashesOf = (pattern: RegExp) => [...html.matchAll(pattern)].map((match) => sha256(match[1]));
      fs.writeFileSync(
        CSP_HASHES_FILE,
        JSON.stringify(
          {
            script: hashesOf(/<script(?![^>]*\bsrc=)[^>]*>([\s\S]*?)<\/script>/g),
            style: hashesOf(/<style[^>]*>([\s\S]*?)<\/style>/g),
          },
          null,
          2,
        ),
      );
    },
  };
}

// Read once at startup: `vite preview` runs after the build, so the file is already there.
// A rebuild while preview is running will not refresh this — restart preview.
function contentSecurityPolicy(): string {
  const hashes: { script: string[]; style: string[] } = fs.existsSync(CSP_HASHES_FILE)
    ? JSON.parse(fs.readFileSync(CSP_HASHES_FILE, 'utf8'))
    : { script: [], style: [] };

  return [
    "default-src 'self'",
    "object-src 'none'",
    "frame-ancestors 'self'",
    // 'wasm-unsafe-eval', not 'unsafe-eval': Ketcher compiles the Indigo WASM module, which
    // is all this buys it. Without it the sketcher mounts but every structure operation dies
    // with `Aborted(CompileError: ...)`.
    ['script-src', "'self'", "'wasm-unsafe-eval'", ...hashes.script].join(' '),
    // The sha256 is the hash of the *empty string*. Emotion (ketcher-react's styling engine,
    // via MUI) is already in its production "speedy" mode: it inserts an empty <style> carrier
    // into <head> and then adds every rule through `CSSStyleSheet.insertRule`, which CSP does
    // not police at all. Only the empty carrier is checked, so this one stable hash admits it —
    // and without it the element is blocked, `tag.sheet` is null, and emotion's `insert` swallows
    // the resulting throw in an empty catch, losing every rule silently (Ketcher renders
    // unstyled). This replaces what would otherwise have to be `style-src 'unsafe-inline'`.
    ['style-src', "'self'", "'sha256-47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU='", ...hashes.style].join(' '),
    // ['style-src-attr', "'self'", "'unsafe-inline'"].join(' '),
    // /api is same-origin through the proxy below; Cognito is the only cross-origin call.
    "connect-src 'self' https://cognito-idp.us-east-1.amazonaws.com",
    // Open Sans ships in the bundle via @fontsource, so no font CDN is needed.
    "font-src 'self'",
    "worker-src 'self' blob:",
    "media-src 'self' data:",
    "img-src 'self' blob: data:",
  ].join('; ');
}

export default defineConfig({
  plugins: [
    tanstackRouter({
      target: 'react',
      autoCodeSplitting: true,
      // Colocated test files live in src/routes/ but are not routes.
      routeFileIgnorePattern: '\\.(test|stories)\\.',
    }),
    react(),
    tailwindcss(),
    cspHashes(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  // Ketcher's bundles were built for webpack and reference the bare `global`.
  define: {
    global: 'globalThis',
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: API_TARGET,
        changeOrigin: true,
        secure: false,
      },
    },
  },
  preview: {
    port: 4173,
    headers: {
      'Content-Security-Policy': contentSecurityPolicy(),
    },
    proxy: {
      '/api': {
        target: API_TARGET,
        changeOrigin: true,
        secure: false,
      },
    },
  },
  test: {
    projects: [
      {
        extends: true,
        test: {
          name: 'unit',
          maxWorkers: MAX_WORKERS,
          environment: 'jsdom',
          globals: true,
          setupFiles: ['./src/test-setup.ts'],
          include: ['src/**/*.test.{ts,tsx}'],
        },
      },
      {
        // Every story runs as a smoke test in real Chromium, with the a11y addon
        // configured in .storybook/preview.tsx failing the test on violations.
        extends: true,
        plugins: [storybookTest({ configDir: '.storybook' })],
        test: {
          name: 'storybook',
          // One page per worker, all in a single Chromium. Left to itself Vitest opens one
          // per core — 14 on a 16-core machine — and those pages then contend for the one
          // browser process they share. Measured on this suite (78 files, 417 tests), the
          // default buys nothing for that: wall clock 58-74s at 14 workers against 54-67s
          // at 4, while cumulative test time goes 190-234s against 75-95s and setup
          // 326-470s against 94-115s. So ~3x the CPU burned for no wall-clock gain, and
          // the tail of that contention landed on whichever story was waiting out a
          // service-worker round trip. Four is still ~2x faster than serial (112s).
          //
          // Machine-dependent, obviously — but the ceiling is the shared browser process,
          // not the core count, so raise this only alongside a measurement.
          maxWorkers: MAX_WORKERS,
          // Storybook 10.3+ can provision preview annotations itself, but only
          // a project setup file gets scanned for dep pre-bundling — without one
          // the CJS deps behind @testing-library/dom fail to import in the browser.
          setupFiles: ['.storybook/vitest.setup.ts'],
          browser: {
            enabled: true,
            headless: true,
            provider: playwright(CHROMIUM_BIN ? { launchOptions: { executablePath: CHROMIUM_BIN } } : {}),
            instances: [{ browser: 'chromium' }],
          },
        },
      },
    ],
  },
});
