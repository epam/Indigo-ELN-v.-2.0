import { fileURLToPath } from 'node:url';

import type { StorybookConfig } from '@storybook/react-vite';

const config: StorybookConfig = {
  stories: ['../src/**/*.stories.@(ts|tsx)'],
  addons: ['@storybook/addon-docs', '@storybook/addon-a11y', '@storybook/addon-vitest', 'msw-storybook-addon'],
  framework: { name: '@storybook/react-vite', options: {} },
  staticDirs: ['../public'],
  viteFinal: (config) => {
    // The TanStack Router codegen plugin watches src/routes and rewrites the
    // gitignored routeTree.gen.ts. Storybook never renders a route, so drop it.
    config.plugins = config.plugins?.filter(
      (plugin) => !(plugin && 'name' in plugin && String(plugin.name).includes('tanstack-router')),
    );
    // Vite merges the inherited '@' -> src alias ahead of anything added here, and a
    // string alias matches by prefix, so an object would never let these two win.
    // Declaring the array outright fixes the order.
    config.resolve ??= {};
    config.resolve.alias = [
      // Stories must never load the real Ketcher: ~28 MB of sketcher and Indigo WASM.
      { find: '@/lib/ketcher', replacement: fileURLToPath(new URL('./mocks/ketcher.ts', import.meta.url)) },
      {
        find: '@/components/chemistry/ketcher-editor',
        replacement: fileURLToPath(new URL('./mocks/ketcher-editor.tsx', import.meta.url)),
      },
      // Stories must never reach Cognito: apiFetch calls fetchAuthSession() before
      // every request and AppSidebar calls signOut(). MSW owns the network instead.
      { find: 'aws-amplify/auth', replacement: fileURLToPath(new URL('./mocks/amplify-auth.ts', import.meta.url)) },
      { find: '@', replacement: fileURLToPath(new URL('../src', import.meta.url)) },
    ];
    return config;
  },
};

export default config;
