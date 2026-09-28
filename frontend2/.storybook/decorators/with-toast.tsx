import {ToastProvider} from '@/components/ui/toast';

import type {Decorator} from '@storybook/react-vite';

/**
 * `apiFetch` raises a toast on every failed request, so any story whose handlers fail
 * needs the provider mounted — without it Base UI throws on the missing context.
 */
export const withToast: Decorator = (Story) => (
  <ToastProvider>
    <Story />
  </ToastProvider>
);
