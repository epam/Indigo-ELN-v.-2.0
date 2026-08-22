import { fileURLToPath } from 'node:url';
import { storybookTest } from '@storybook/addon-vitest/vitest-plugin';
import tailwindcss from '@tailwindcss/vite';
import { tanstackRouter } from '@tanstack/router-plugin/vite';
import react from '@vitejs/plugin-react';
import { playwright } from '@vitest/browser-playwright';
import { defineConfig } from 'vitest/config';

// The deployed app is served as static files from S3/CloudFront, where /api is
// same-origin (CloudFront forwards it to the API Gateway origin). Only the dev
// server needs a proxy.
//
// The target must be the CloudFront domain, not the API Gateway URL directly:
// CloudFront injects the X-API-Secret header the API origin expects
// (CloudFrontStack.java, apiBehavior).
const API_TARGET = 'https://indigo-eln-dev.test.lifescience.opensource.epam.com';

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
  test: {
    projects: [
      {
        extends: true,
        test: {
          name: 'unit',
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
          // Storybook 10.3+ can provision preview annotations itself, but only
          // a project setup file gets scanned for dep pre-bundling — without one
          // the CJS deps behind @testing-library/dom fail to import in the browser.
          setupFiles: ['.storybook/vitest.setup.ts'],
          browser: {
            enabled: true,
            headless: true,
            provider: playwright(),
            instances: [{ browser: 'chromium' }],
          },
        },
      },
    ],
  },
});
