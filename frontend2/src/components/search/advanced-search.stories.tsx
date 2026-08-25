import { useState } from 'react';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { AdvancedSearch } from '@/components/search/advanced-search';
import { EMPTY_GLOBAL_SEARCH_FORM, type GlobalSearchFormValues } from '@/components/search/global-search-form';
import { lookupErrorHandlers, slowLookupHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

function AdvancedSearchHarness({
  initial = {},
  defaultOpen = false,
}: {
  initial?: Partial<GlobalSearchFormValues>;
  defaultOpen?: boolean;
}) {
  const [values, setValues] = useState<GlobalSearchFormValues>({ ...EMPTY_GLOBAL_SEARCH_FORM, ...initial });
  const [open, setOpen] = useState(defaultOpen);

  return (
    <div className="w-[720px] bg-neutral-100 p-4">
      <AdvancedSearch
        values={values}
        onChange={(patch) => setValues((previous) => ({ ...previous, ...patch }))}
        open={open}
        onOpenChange={setOpen}
      />
    </div>
  );
}

const meta = {
  title: 'Search/AdvancedSearch',
  component: AdvancedSearchHarness,
  args: {},
} satisfies Meta<typeof AdvancedSearchHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Collapsed and untouched: just the title and its chevron. */
export const Collapsed: Story = {};

export const Expanded: Story = {
  args: { defaultOpen: true },
};

/** Reaction Role only exists for a drawn molecule. */
export const WithMoleculeStructure: Story = {
  args: { defaultOpen: true, initial: { structure: 'molfile' } },
  play: async ({ canvasElement }) => {
    await expect(within(canvasElement).getByLabelText('Reaction Role')).toBeInTheDocument();
  },
};

/** A drawn reaction goes to reactionStructure, which the backend pairs with no role. */
export const WithReactionStructure: Story = {
  args: { defaultOpen: true, initial: { structure: 'rxnfile', isReaction: true } },
  play: async ({ canvasElement }) => {
    await expect(within(canvasElement).queryByLabelText('Reaction Role')).not.toBeInTheDocument();
  },
};

/** What the header says once collapsed again. */
export const CollapsedSummary: Story = {
  args: {
    initial: {
      therapeuticArea: { id: 'ta-1', name: 'Obesity' },
      projectCode: { id: 'pc-1', name: 'Code 1' },
      batchYield: { type: 'ge', value: 90 },
      author: [{ username: 'mark.liu', displayName: 'Mark Liu' }],
      experimentStatus: 'SIGNED',
    },
  },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByText('Batch Yield, %')).toBeInTheDocument();
    await expect(canvas.getByText('≥')).toBeInTheDocument();
    await expect(canvas.getByText('Obesity')).toBeInTheDocument();
    await expect(canvas.getByText('Mark Liu')).toBeInTheDocument();

    // Expanding hands the job back to the controls, so the summary steps aside. Probed
    // with a value rather than an operator: '≥' comes back as the numeric operator button,
    // whereas 'Obesity' becomes an input value, which getByText does not match.
    await userEvent.click(canvas.getByRole('button', { name: /Advanced Search/ }));
    await expect(canvas.queryByText('Obesity')).not.toBeInTheDocument();
  },
};

/** Picking a therapeutic area, end to end through the MSW dictionary handler. */
export const PicksATherapeuticArea: Story = {
  args: { defaultOpen: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.type(canvas.getByLabelText('Therapeutic Area'), 'obe');
    await userEvent.click(await screen.findByRole('option', { name: 'Obesity' }));

    await userEvent.click(canvas.getByRole('button', { name: /Advanced Search/ }));
    await expect(canvas.getByText('Obesity')).toBeInTheDocument();
  },
};

/** Author suggestions come from the server, so they are the one debounced lookup here. */
export const SuggestsAuthors: Story = {
  args: { defaultOpen: true },
  play: async ({ canvasElement }) => {
    await userEvent.type(within(canvasElement).getByLabelText('Author'), 'Mark');
    await userEvent.click(await screen.findByRole('option', { name: 'Mark Liu' }));
    await expect(await screen.findByRole('button', { name: 'Remove Mark Liu' })).toBeInTheDocument();
  },
};

/** The signed-in user is one click away, and only offered once. */
export const AddsMeAsAnAuthor: Story = {
  args: { defaultOpen: true },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const addMe = canvas.getByRole('button', { name: 'Add Me as an Author' });
    // Disabled until currentUser resolves — and a disabled Button has pointer-events: none.
    await waitFor(() => expect(addMe).toBeEnabled());
    await userEvent.click(addMe);
    await expect(canvas.getByRole('button', { name: 'Remove Administrator' })).toBeInTheDocument();
    await expect(addMe).toBeDisabled();
  },
};

export const SlowLookups: Story = {
  args: { defaultOpen: true },
  parameters: { msw: { handlers: slowLookupHandlers } },
};

/** apiFetch toasts the failure; the popups only explain why they are empty. */
export const LookupsFailed: Story = {
  args: { defaultOpen: true },
  parameters: { msw: { handlers: lookupErrorHandlers } },
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getAllByRole('button', { name: 'Show options' })[0]);
    await expect(await screen.findByText('Could not load options')).toBeInTheDocument();
  },
};
