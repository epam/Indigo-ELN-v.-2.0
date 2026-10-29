import {ToastProvider} from '@/components/ui/toast';

import type {Decorator} from '@storybook/react-vite';

/**
 * Every failed query and mutation raises a toast, so any story whose handlers fail
 * needs the provider mounted — without it Base UI throws on the missing context.
 */
export const withToast: Decorator = (Story) => (
  <ToastProvider>
    <Story />
  </ToastProvider>
);
