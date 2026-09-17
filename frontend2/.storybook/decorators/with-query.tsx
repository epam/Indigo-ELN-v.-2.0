import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { useState } from 'react';

import type { Decorator } from '@storybook/react-vite';

/**
 * A fresh QueryClient per story — the shared client in src/lib/query-client.ts
 * would leak cached data between stories and make loading states unreachable.
 */
function QueryScope({ children }: { children: ReactNode }) {
  const [client] = useState(
    () =>
      new QueryClient({
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
