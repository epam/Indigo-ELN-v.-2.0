import { useState } from 'react';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { GlobalSearchPanel } from '@/components/search/global-search-panel';

import type { GlobalSearchRequest } from '@/lib/types/search.ts';
import type { Meta, StoryObj } from '@storybook/react-vite';

function GlobalSearchPanelHarness({ initialQuery }: { initialQuery: string }) {
  const [open, setOpen] = useState(true);
  const [query, setQuery] = useState(initialQuery);
  const [request, setRequest] = useState<GlobalSearchRequest | null>(null);
  return (
    <>
      <pre role="status" className="text-[12px]">
        {request ? JSON.stringify(request) : 'Not searched yet.'}
      </pre>
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

/**
 * An advanced filter alone is enough to search — the backend accepts a request with no
 * term and no structure, so the button must not stay disabled.
 */
export const SearchesOnAnAdvancedFilterAlone: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(screen.getByRole('button', { name: 'Search' })).toBeDisabled();

    await userEvent.click(screen.getByRole('button', { name: /Advanced Search/ }));
    await userEvent.type(screen.getByLabelText('Therapeutic Area'), 'obe');
    await userEvent.click(await screen.findByRole('option', { name: 'Obesity' }));

    const search = screen.getByRole('button', { name: 'Search' });
    await expect(search).toBeEnabled();
    await userEvent.click(search);

    // Searching collapses the panel back to its summary.
    await waitFor(() => expect(screen.queryByLabelText('Therapeutic Area')).not.toBeInTheDocument());
    // hidden: true because the open dialog inerts the page behind it, harness included.
    await expect(JSON.parse(canvas.getByRole('status', { hidden: true }).textContent ?? '{}')).toMatchObject({
      query: null,
      therapeuticArea: { name: 'Obesity' },
    });
  },
};

/** Clear All has to reach the advanced fields too, including their own local state. */
export const ClearAllResetsAdvancedFields: Story = {
  play: async () => {
    await userEvent.click(screen.getByRole('button', { name: /Advanced Search/ }));
    await userEvent.type(screen.getByLabelText('Therapeutic Area'), 'obe');
    await userEvent.click(await screen.findByRole('option', { name: 'Obesity' }));
    await expect(screen.getByLabelText('Therapeutic Area')).toHaveValue('Obesity');

    await userEvent.click(screen.getByRole('button', { name: 'Clear All' }));
    await waitFor(() => expect(screen.getByRole('button', { name: 'Search' })).toBeDisabled());
  },
};

/** Pressing Search sends the request and renders the hits below the form. */
export const RunsASearch: Story = {
  args: { initialQuery: 'coupling' },
  play: async () => {
    await userEvent.click(screen.getByRole('button', { name: 'Search' }));
    await expect(await screen.findByText('Search Results (27)')).toBeInTheDocument();
    await expect(screen.getByText('Suzuki coupling of aryl bromide')).toBeInTheDocument();
  },
};

/** Clear All drops the results along with the criteria that produced them. */
export const ClearAllDropsResults: Story = {
  args: { initialQuery: 'coupling' },
  play: async () => {
    await userEvent.click(screen.getByRole('button', { name: 'Search' }));
    await expect(await screen.findByText('Search Results (27)')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'Clear All' }));
    await waitFor(() => expect(screen.queryByText('Search Results (27)')).not.toBeInTheDocument());
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
