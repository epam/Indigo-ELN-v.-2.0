import { http, HttpResponse } from 'msw';
import { useState } from 'react';
import { expect, screen, userEvent, waitFor } from 'storybook/test';

import { NotebookFormDialog } from '@/components/notebooks/notebook-form-dialog';
import { makeNotebookDetails } from '@/mocks/fixtures';
import {
  createNotebookErrorHandlers,
  handlers,
  initializingNotebookHandlers,
  NEXT_NOTEBOOK_NAME,
  nextNumberErrorHandlers,
  TAKEN_NOTEBOOK_NAME,
} from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';
import type { NotebookDetails } from '@/lib/types/notebooks.ts';

const NOTEBOOK = makeNotebookDetails();

/** `null` is how a story asks for create mode — plain `undefined` would take the default. */
function NotebookFormDialogHarness({ notebook = NOTEBOOK }: { notebook?: NotebookDetails | null }) {
  const [open, setOpen] = useState(true);
  return (
    <>
      {!open && <p className="text-[14px]/6">Dialog closed.</p>}
      <NotebookFormDialog
        open={open}
        onOpenChange={setOpen}
        projectId={NOTEBOOK.projectId}
        notebook={notebook ?? undefined}
      />
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

/**
 * Create mode. The name is not typed but fetched: the dialog opens on
 * `/notebooks/next-number` and seeds the field with what it answers.
 */
export const Create: Story = {
  render: () => <NotebookFormDialogHarness notebook={null} />,
  play: async () => {
    await expect(await screen.findByRole('heading', { name: 'Add Notebook' })).toBeInTheDocument();
    await waitFor(() => expect(screen.getByLabelText(/Notebook Name/)).toHaveValue(NEXT_NOTEBOOK_NAME));
  },
};

/**
 * The initializing phase, pinned on a next-number request that never answers: the body is inert
 * under a spinner and Save is disabled, but the dialog is not — Cancel still closes it, since
 * nothing has been typed that abandoning would lose.
 */
export const CreateInitializing: Story = {
  parameters: { msw: { handlers: initializingNotebookHandlers } },
  render: () => <NotebookFormDialogHarness notebook={null} />,
  play: async () => {
    await expect(await screen.findByRole('status')).toHaveTextContent('Loading…');
    await expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled();
    await expect(screen.getByLabelText(/Notebook Name/)).toHaveValue('');
    await userEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    await expect(await screen.findByText('Dialog closed.')).toBeInTheDocument();
  },
};

/**
 * A failed next-number leaves the field blank and editable rather than blocking the dialog —
 * `apiFetch` has already reported the failure, and the name was always the user's to type.
 */
export const CreateNextNumberFails: Story = {
  parameters: { msw: { handlers: nextNumberErrorHandlers } },
  render: () => <NotebookFormDialogHarness notebook={null} />,
  play: async () => {
    const input = await screen.findByLabelText(/Notebook Name/);
    // The initializing phase freezes the body with `inert`, which leaves no `disabled` attribute
    // to wait on — the spinner's status region going away is the observable end of it.
    await waitFor(() => expect(screen.queryByText('Loading…')).not.toBeInTheDocument());
    await expect(input).toHaveValue('');
    await userEvent.type(input, '00000042');
    await expect(input).toHaveValue('00000042');
  },
};

/** Creating closes the dialog and routes to the new notebook. */
export const CreateSave: Story = {
  render: () => <NotebookFormDialogHarness notebook={null} />,
  play: async () => {
    await waitFor(() => expect(screen.getByLabelText(/Notebook Name/)).toHaveValue(NEXT_NOTEBOOK_NAME));
    const save = screen.getByRole('button', { name: 'Save' });
    await waitFor(() => expect(save).toBeEnabled(), { timeout: 3000 });
    await userEvent.click(save);
    await expect(await screen.findByText('Dialog closed.')).toBeInTheDocument();
  },
};

/** A failed POST leaves the dialog open with what was typed still in it. */
export const CreateFails: Story = {
  parameters: { msw: { handlers: createNotebookErrorHandlers } },
  render: () => <NotebookFormDialogHarness notebook={null} />,
  play: async () => {
    const input = await screen.findByLabelText(/Notebook Name/);
    await waitFor(() => expect(input).toHaveValue(NEXT_NOTEBOOK_NAME));
    const save = screen.getByRole('button', { name: 'Save' });
    await waitFor(() => expect(save).toBeEnabled(), { timeout: 3000 });
    await userEvent.click(save);
    await waitFor(() => expect(screen.getByRole('heading', { name: 'Add Notebook' })).toBeInTheDocument());
    await expect(input).toHaveValue(NEXT_NOTEBOOK_NAME);
  },
};
