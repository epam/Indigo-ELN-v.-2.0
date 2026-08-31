import { http, HttpResponse } from 'msw';
import { useState } from 'react';
import { expect, screen, userEvent, waitFor } from 'storybook/test';

import { NotebookFormDialog } from '@/components/notebooks/notebook-form-dialog';
import { makeNotebookDetails } from '@/mocks/fixtures';
import { handlers, TAKEN_NOTEBOOK_NAME } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { NotebookDetails } from '@/lib/types/notebooks.ts';

const NOTEBOOK = makeNotebookDetails();

function NotebookFormDialogHarness({ notebook = NOTEBOOK }: { notebook?: NotebookDetails }) {
  const [open, setOpen] = useState(true);
  return (
    <>
      {!open && <p className="text-[14px]/6">Dialog closed.</p>}
      <NotebookFormDialog open={open} onOpenChange={setOpen} notebook={notebook} />
    </>
  );
}

/** Records what actually reached the wire, so a story can assert an untouched Save sends {}. */
const patchBodies: unknown[] = [];

const patchSpyHandlers = [
  http.patch('/api/eln/notebooks/:id', async ({ request, params }) => {
    patchBodies.push(await request.json());
    return HttpResponse.json(makeNotebookDetails({ id: String(params.id) }));
  }),
  ...handlers,
];

const meta = {
  title: 'Notebooks/NotebookFormDialog',
  component: NotebookFormDialogHarness,
} satisfies Meta<typeof NotebookFormDialogHarness>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Seeded from the notebook as stored, so Save with no edits sends an empty body. */
export const Default: Story = {
  play: async () => {
    await expect(screen.getByLabelText(/Notebook Name/)).toHaveValue(NOTEBOOK.name);
  },
};

/** A notebook is numbered, not named — anything but eight digits is rejected. */
export const NonNumericName: Story = {
  play: async () => {
    const input = screen.getByLabelText(/Notebook Name/);
    await userEvent.clear(input);
    await userEvent.type(input, '0000000a');
    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('Use 8 digits only'));
  },
};

/** Too few digits is the same rule, and reports before any request is made. */
export const TooShortName: Story = {
  play: async () => {
    const input = screen.getByLabelText(/Notebook Name/);
    await userEvent.clear(input);
    await userEvent.type(input, '123');
    await waitFor(() => expect(screen.getByRole('alert')).toHaveTextContent('Use 8 digits only'));
  },
};

/** The name is checked against /notebooks/existence as it is typed. */
export const DuplicateName: Story = {
  play: async () => {
    const input = screen.getByLabelText(/Notebook Name/);
    await userEvent.clear(input);
    await userEvent.type(input, TAKEN_NOTEBOOK_NAME);
    await waitFor(
      () => expect(screen.getByRole('alert')).toHaveTextContent(`Notebook '${TAKEN_NOTEBOOK_NAME}' already exists`),
      {
        timeout: 3000,
      },
    );
  },
};

/** Saving closes the dialog; the PATCH answers with the whole notebook. */
export const Save: Story = {
  play: async () => {
    const input = screen.getByLabelText(/Notebook Name/);
    await userEvent.clear(input);
    await userEvent.type(input, '00000042');
    // Save is held disabled while the uniqueness check is in flight, which is the point of
    // `state.isValidating` — so the click has to wait for it rather than race it.
    const save = screen.getByRole('button', { name: 'Save' });
    await waitFor(() => expect(save).toBeEnabled(), { timeout: 3000 });
    await userEvent.click(save);
    await expect(await screen.findByText('Dialog closed.')).toBeInTheDocument();
  },
};

/**
 * **Regression guard.** Opening the dialog on a description the editor has to normalise — plain
 * text, a `<div>`, pretty-printed HTML — and pressing Save without touching anything must send an
 * empty body. It used to send a normalised copy of the untouched text, which can clobber an edit
 * someone else made in the meantime; omitting untouched fields is exactly what prevents that.
 */
export const UntouchedSaveSendsNothing: Story = {
  parameters: { msw: { handlers: patchSpyHandlers } },
  render: () => <NotebookFormDialogHarness notebook={makeNotebookDetails({ description: 'Aspirin route A.' })} />,
  play: async () => {
    patchBodies.length = 0;
    await userEvent.click(await screen.findByRole('button', { name: 'Save' }));
    await waitFor(() => expect(patchBodies).toEqual([{}]));
  },
};
