import { expect, within } from 'storybook/test';

import { SignatureCollection } from '@/components/signatures/signature-collection';
import { emptyHandlers, errorHandlers, loadingHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Signatures/SignatureCollection',
  component: SignatureCollection,
  args: {
    filters: { search: '', sort: 'LATEST', waitingMySignature: false },
  },
} satisfies Meta<typeof SignatureCollection>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** The toggle's filter — only documents the signed-in user is a signer on. */
export const MySignaturesOnly: Story = {
  args: { filters: { search: '', sort: 'LATEST', waitingMySignature: true } },
};

export const Loading: Story = {
  parameters: { msw: { handlers: loadingHandlers } },
};

export const Empty: Story = {
  parameters: { msw: { handlers: emptyHandlers } },
};

/** apiFetch toasts the failure; this explains the missing list, in the same words. */
export const Error: Story = {
  parameters: { msw: { handlers: errorHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByText(/Could not load signatures/)).toBeInTheDocument();
  },
};
