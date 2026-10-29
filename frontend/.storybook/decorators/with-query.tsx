import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { useState } from 'react';

import { createCaches } from '@/lib/query-client';

import type { Decorator } from '@storybook/react-vite';

/**
 * A fresh QueryClient per story — the shared client in src/lib/query-client.ts
 * would leak cached data between stories and make loading states unreachable. Its caches are
 * the app's, so a failure toasts in a story exactly as it would on the page.
 */
function QueryScope({ children }: { children: ReactNode }) {
  const [client] = useState(
    () =>
      new QueryClient({
        ...createCaches(),
        defaultOptions: { queries: { retry: false, staleTime: Infinity } },
      }),
  );

  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

export const withQuery: Decorator = (Story) => (
  <QueryScope>
    <Story />
  </QueryScope>
);
