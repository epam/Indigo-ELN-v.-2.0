import { http, HttpResponse } from 'msw';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { UndoRedoButtons } from '@/components/experiments/experiment-actions';
import { Input } from '@/components/ui/input';
import { makeExperimentDetails } from '@/mocks/fixtures';
import { handlers, nothingToUndoHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const EXPERIMENT = makeExperimentDetails();

/** Records the mutations that actually reached the wire, so a story can assert the payload. */
const sent: unknown[] = [];

const spyHandlers = [
  http.post('/api/eln/experiments/:id/mutate', async ({ request }) => {
    sent.push(await request.json());
    return HttpResponse.json({ patch: {} });
  }),
  ...handlers,
];

const meta = {
  title: 'Experiments/UndoRedoButtons',
  component: UndoRedoButtons,
  args: { experiment: EXPERIMENT, saving: false },
  parameters: { msw: { handlers: spyHandlers } },
} satisfies Meta<typeof UndoRedoButtons>;

export default meta;
type Story = StoryObj<typeof meta>;

/**
 * Both enabled. There is no `canUndo` to read, so an enabled button says only that the
 * experiment is writable — pressing with an empty stack is what asks the server.
 */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Undo' })).toBeEnabled();
    await expect(canvas.getByRole('button', { name: 'Redo' })).toBeEnabled();
  },
};

/** The two payloads, in the order they were pressed. */
export const SendsUndoAndRedo: Story = {
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(canvas.getByRole('button', { name: 'Undo' }));
    await waitFor(() => expect(sent).toEqual([{ type: 'Undo' }]));

    await userEvent.click(canvas.getByRole('button', { name: 'Redo' }));
    await waitFor(() => expect(sent).toEqual([{ type: 'Undo' }, { type: 'Redo' }]));
  },
};

/**
 * Ctrl+Z, Ctrl+Y and Ctrl+Shift+Z, dispatched at the page rather than at either button —
 * the listener is on `document`, since the shortcut belongs to the page. Cmd+Y is not redo
 * on macOS, which is why the third chord exists.
 */
export const KeyboardShortcuts: Story = {
  play: async () => {
    sent.length = 0;

    await userEvent.keyboard('{Control>}z{/Control}');
    await waitFor(() => expect(sent).toEqual([{ type: 'Undo' }]));

    await userEvent.keyboard('{Control>}y{/Control}');
    await waitFor(() => expect(sent).toEqual([{ type: 'Undo' }, { type: 'Redo' }]));

    await userEvent.keyboard('{Control>}{Shift>}z{/Shift}{/Control}');
    await waitFor(() => expect(sent).toEqual([{ type: 'Undo' }, { type: 'Redo' }, { type: 'Redo' }]));
  },
};

/**
 * A caret in a text field owns Ctrl+Z itself — as do the rich-text editor and the sketcher,
 * which the handler skips the same way. Undoing the typing must not undo the experiment.
 */
export const IgnoresShortcutsInFields: Story = {
  render: (args) => (
    <div className="flex items-center gap-4">
      <Input aria-label="Chemical Name" defaultValue="Acetic anhydride" />
      <UndoRedoButtons {...args} />
    </div>
  ),
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(canvas.getByLabelText('Chemical Name'));
    await userEvent.keyboard('{Control>}z{/Control}');

    // Nothing to wait for, so give the request the chance to be made and assert it was not.
    await new Promise((resolve) => setTimeout(resolve, 100));
    await expect(sent).toEqual([]);
  },
};

/**
 * A signed experiment is read-only to its own author, so both buttons are disabled and the
 * shortcuts are inert — `canEditExperiment` is permission *and* status, and the backend
 * rejects a mutation on either count.
 */
export const ReadOnly: Story = {
  args: { experiment: makeExperimentDetails({ status: 'SIGNED' }) },
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    await expect(canvas.getByRole('button', { name: 'Undo' })).toBeDisabled();
    await expect(canvas.getByRole('button', { name: 'Redo' })).toBeDisabled();

    await userEvent.keyboard('{Control>}z{/Control}');
    await new Promise((resolve) => setTimeout(resolve, 100));
    await expect(sent).toEqual([]);
  },
};

/**
 * The empty-stack answer: 400 with the backend's own wording, toasted centrally by `apiFetch`.
 * That is the whole error path — nothing here tracks a local stack to grey the button out.
 */
export const NothingToUndo: Story = {
  parameters: { msw: { handlers: nothingToUndoHandlers } },
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Undo' }));

    // Portalled, and Base UI marks an unfocused toast aria-hidden — so query the text, not a role.
    await expect(await screen.findByText('Nothing to undo')).toBeInTheDocument();
  },
};
