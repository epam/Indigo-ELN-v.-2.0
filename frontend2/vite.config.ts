import { fileURLToPath } from 'node:url';
import tailwindcss from '@tailwindcss/vite';
import { tanstackRouter } from '@tanstack/router-plugin/vite';
import react from '@vitejs/plugin-react';
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
  plugins: [tanstackRouter({ target: 'react', autoCodeSplitting: true }), react(), tailwindcss()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
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
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test-setup.ts'],
  },
});
