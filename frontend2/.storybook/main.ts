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
    // Stories must never reach Cognito: apiFetch calls fetchAuthSession() before
    // every request and AppSidebar calls signOut(). MSW owns the network instead.
    config.resolve ??= {};
    config.resolve.alias = {
      ...config.resolve.alias,
      'aws-amplify/auth': fileURLToPath(new URL('./mocks/amplify-auth.ts', import.meta.url)),
    };
    return config;
  },
};

export default config;
