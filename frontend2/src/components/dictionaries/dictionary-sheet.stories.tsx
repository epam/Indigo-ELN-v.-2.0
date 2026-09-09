import { expect, fn, screen, userEvent } from 'storybook/test';

import { DictionarySheet } from '@/components/dictionaries/dictionary-sheet';
import { DICTIONARY_LIST } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const [HANDLING, COMPOUND] = DICTIONARY_LIST;

const meta = {
  title: 'Dictionaries/DictionarySheet',
  component: DictionarySheet,
  args: { dictionary: COMPOUND, onClose: () => {} },
} satisfies Meta<typeof DictionarySheet>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  // The sheet is portalled, so it is outside canvasElement — reached with `screen`.
  play: async () => {
    await expect(await screen.findByText('About Dictionary')).toBeInTheDocument();
    await expect(screen.getByText(COMPOUND.description)).toBeInTheDocument();
  },
};

/** `description` is empty in the seed data for several dictionaries. */
export const NoDescription: Story = {
  args: { dictionary: HANDLING },
  play: async () => {
    await expect(await screen.findByText('—')).toBeInTheDocument();
  },
};

/**
 * Shut, and mounted anyway. A sheet behind a `{open && …}` flag cannot animate out, so the route
 * renders it unconditionally and lets `dictionary` drive it.
 */
export const Closed: Story = {
  args: { dictionary: undefined },
  play: async () => {
    await expect(screen.queryByText('About Dictionary')).not.toBeInTheDocument();
  },
};

/** Closing is the route's business — the sheet only reports it, so the search param can go. */
export const Closes: Story = {
  args: { onClose: fn() },
  play: async ({ args }) => {
    await userEvent.click(await screen.findByRole('button', { name: 'Close' }));
    await expect(args.onClose).toHaveBeenCalled();
  },
};
