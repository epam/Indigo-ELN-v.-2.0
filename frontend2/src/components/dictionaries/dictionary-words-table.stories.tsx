import { expect, userEvent, waitFor, within } from 'storybook/test';

import { DictionaryWordsTable } from '@/components/dictionaries/dictionary-words-table';
import { DICTIONARY_ITEMS, DICTIONARY_LIST } from '@/mocks/fixtures';
import { emptyHandlers, errorHandlers, loadingHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const [, COMPOUND] = DICTIONARY_LIST;
const [FIRST, INACTIVE] = DICTIONARY_ITEMS;

const meta = {
  title: 'Dictionaries/DictionaryWordsTable',
  component: DictionaryWordsTable,
  args: { dictionary: COMPOUND },
} satisfies Meta<typeof DictionaryWordsTable>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Every column the backend lets an admin change, plus the one inactive word `/full` returns. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByLabelText(`${FIRST.name} is active`)).toBeChecked();
    await expect(canvas.getByLabelText(`${INACTIVE.name} is active`)).not.toBeChecked();
  },
};

export const Loading: Story = { parameters: { msw: { handlers: loadingHandlers } } };

export const Empty: Story = { parameters: { msw: { handlers: emptyHandlers } } };

export const Error: Story = {
  parameters: { msw: { handlers: errorHandlers } },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(await canvas.findByText(/Could not load dictionary words/)).toBeInTheDocument();
  },
};

/** Name only, matching indigo-frontend's filter — and derived, so a write cannot reset it. */
export const Filtered: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await canvas.findByLabelText(`Name of ${FIRST.name}`);

    await userEvent.type(canvas.getByLabelText('Search words'), 'gloves');

    await waitFor(() => expect(canvas.queryByLabelText(`Name of ${FIRST.name}`)).not.toBeInTheDocument());
    await expect(canvas.getByLabelText('Name of Gloves required')).toBeInTheDocument();
  },
};

/**
 * The indigo-frontend gesture: a blank row appears with its name focused, and leaving the field
 * commits it. Asserts on the POST reaching the handler, not on the row that the cache round trip
 * eventually paints.
 */
export const AddWord: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await canvas.findByLabelText(`Name of ${FIRST.name}`);

    await userEvent.click(canvas.getByRole('button', { name: 'Add Word' }));
    const input = await canvas.findByLabelText('New word name');
    await expect(input).toHaveFocus();

    await userEvent.type(input, 'Air sensitive');
    await userEvent.tab();

    await waitFor(() => expect(canvas.getByLabelText('Name of Air sensitive')).toBeInTheDocument());
  },
};

/** A name that already exists is refused in the row, without spending a request to be told so. */
export const AddWordRejectsDuplicate: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await canvas.findByLabelText(`Name of ${FIRST.name}`);

    await userEvent.click(canvas.getByRole('button', { name: 'Add Word' }));
    const input = await canvas.findByLabelText('New word name');
    // Case-insensitively, as the backend's unique index is.
    await userEvent.type(input, FIRST.name.toUpperCase());
    await userEvent.tab();

    await waitFor(() => expect(input).toHaveAttribute('aria-invalid', 'true'));
    await expect(input).toHaveAttribute('title', 'Name must be unique. Please choose a different name.');
  },
};

/** Escape drops the row; a row opened by mistake should not need a name to get rid of. */
export const AddWordCancelled: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await canvas.findByLabelText(`Name of ${FIRST.name}`);

    await userEvent.click(canvas.getByRole('button', { name: 'Add Word' }));
    await userEvent.type(await canvas.findByLabelText('New word name'), 'Discarded{Escape}');

    await waitFor(() => expect(canvas.queryByLabelText('New word name')).not.toBeInTheDocument());
  },
};

/** The column indigo-frontend renders and never wires up. */
export const ToggleActive: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const checkbox = await canvas.findByLabelText(`${FIRST.name} is active`);

    await userEvent.click(checkbox);

    await waitFor(() => expect(canvas.getByLabelText(`${FIRST.name} is active`)).not.toBeChecked());
  },
};

/** Renaming an existing word — the whole thing indigo-frontend's table cannot do. */
export const RenameWord: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const name = await canvas.findByLabelText(`Name of ${FIRST.name}`);

    await userEvent.clear(name);
    await userEvent.type(name, 'Severely toxic');
    await userEvent.tab();

    await waitFor(() => expect(canvas.getByLabelText('Name of Severely toxic')).toBeInTheDocument());
  },
};

/** A rank outside the list is refused in the cell rather than sent for the server to reject. */
export const RankOutOfRange: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const rank = await canvas.findByLabelText(`Rank of ${FIRST.name}`);

    await userEvent.clear(rank);
    await userEvent.type(rank, '99');
    await userEvent.tab();

    await waitFor(() => expect(rank).toHaveAttribute('aria-invalid', 'true'));
    await expect(rank).toHaveAttribute('title', `Rank must be between 1 and ${DICTIONARY_ITEMS.length}.`);
  },
};
