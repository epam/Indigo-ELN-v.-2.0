import { useState } from 'react';
import { expect, screen, userEvent, waitFor } from 'storybook/test';

import { GlobalSearchPanel } from '@/components/search/global-search-panel';

import type { GlobalSearchRequest } from '@/lib/types/search.ts';
import type { Meta, StoryObj } from '@storybook/react-vite';

function GlobalSearchPanelHarness({ initialQuery }: { initialQuery: string }) {
  const [open, setOpen] = useState(true);
  const [query, setQuery] = useState(initialQuery);
  const [request, setRequest] = useState<GlobalSearchRequest | null>(null);
  return (
    <>
      <pre className="text-[12px]">{request ? JSON.stringify(request) : 'Not searched yet.'}</pre>
      <GlobalSearchPanel
        open={open}
        onOpenChange={setOpen}
        query={query}
        onQueryChange={setQuery}
        onSearch={setRequest}
      />
    </>
  );
}

const meta = {
  title: 'Search/GlobalSearchPanel',
  component: GlobalSearchPanelHarness,
  args: { initialQuery: '' },
  parameters: { layout: 'fullscreen' },
} satisfies Meta<typeof GlobalSearchPanelHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/** Opened from the header with a term already typed there. */
export const SeededFromHeader: Story = {
  args: { initialQuery: 'aspirin' },
  play: async () => {
    await expect(screen.getByLabelText('Quick search')).toHaveValue('aspirin');
  },
};

/** The search-type choice only appears once there is a structure to match against. */
export const WithStructure: Story = {
  play: async () => {
    await expect(screen.queryByRole('radiogroup')).not.toBeInTheDocument();
    await userEvent.click(screen.getByRole('button', { name: 'Draw Structure' }));
    const save = await screen.findByRole('button', { name: 'Save' });
    await waitFor(() => expect(save).toBeEnabled());
    await userEvent.click(save);
    await expect(await screen.findByRole('radio', { name: 'Substructure' })).toBeChecked();
  },
};
