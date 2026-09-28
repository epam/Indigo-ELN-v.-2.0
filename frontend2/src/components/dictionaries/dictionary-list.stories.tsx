import { useState } from 'react';
import { expect, within } from 'storybook/test';

import { DictionaryList } from '@/components/dictionaries/dictionary-list';
import { useDictionaries } from '@/lib/api/dictionaries';
import { emptyHandlers, errorHandlers, loadingHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

/**
 * The list takes its query as a prop — the route owns it, because it needs the same rows to
 * resolve which dictionary its search param names. This stands in for the route.
 */
function DictionaryListHarness() {
  const [selectedId, setSelectedId] = useState<string | undefined>(undefined);
  return (
    <DictionaryList
      query={useDictionaries()}
      selectedId={selectedId}
      onSelect={(dictionary) => setSelectedId(dictionary.id)}
    />
  );
}

const meta = {
  title: 'Dictionaries/DictionaryList',
  component: DictionaryListHarness,
} satisfies Meta<typeof DictionaryListHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const Loading: Story = { parameters: { msw: { handlers: loadingHandlers } } };

export const Empty: Story = { parameters: { msw: { handlers: emptyHandlers } } };

/** apiFetch toasts the failure; this explains the missing list, in the same words. */
export const Error: Story = {
  parameters: { msw: { handlers: errorHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByText(/Could not load dictionaries/)).toBeInTheDocument();
    // The wording comes from describeError, not from ApiError's constructor string.
    await expect(canvas.queryByText(/Request failed with status/)).not.toBeInTheDocument();
  },
};
