import { http, HttpResponse } from 'msw';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { ExperimentDescriptionPanel } from '@/components/experiments/template/experiment-description-panel';
import { useExperiment } from '@/lib/api/experiments';
import { makeExperimentDetails } from '@/mocks/fixtures';
import { handlers, slowExperimentWriteHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const EXPERIMENT = makeExperimentDetails();

/** Records what actually reached the wire, so a story can assert that nothing did. */
const writtenDescriptions: unknown[] = [];

const writeSpyHandlers = [
  http.patch('/api/eln/experiments/:id', async ({ request, params }) => {
    const body = (await request.json()) as { description?: string };
    writtenDescriptions.push(body);
    // Echoed back rather than answered with the untouched fixture, so a story reading the
    // experiment out of the cache afterwards sees what it just saved.
    return HttpResponse.json(makeExperimentDetails({ id: String(params.id), ...body }));
  }),
  ...handlers,
];

const meta = {
  title: 'Experiments/Template/ExperimentDescriptionPanel',
  component: ExperimentDescriptionPanel,
  args: { experiment: EXPERIMENT },
} satisfies Meta<typeof ExperimentDescriptionPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

/** The editor is the field — there is no edit mode to enter first. */
export const Default: Story = {};

/** Nothing written yet, so the placeholder shows. */
export const Empty: Story = {
  args: { experiment: makeExperimentDetails({ description: undefined }) },
};

/** A reader who cannot edit gets the rendered HTML, not an editor. */
export const ReadOnly: Story = {
  args: { experiment: makeExperimentDetails({ currentPermissions: ['VIEW_EXPERIMENTS'] }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.queryByRole('textbox')).not.toBeInTheDocument();
    await expect(canvas.getByText(/Acetylation of salicylic acid/)).toBeInTheDocument();
  },
};

/** The other half of the gate: EDIT_EXPERIMENTS holds, but a completed experiment is not writable. */
export const Completed: Story = {
  args: { experiment: makeExperimentDetails({ status: 'COMPLETED' }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.queryByRole('textbox')).not.toBeInTheDocument();
    await expect(canvas.getByText(/Acetylation of salicylic acid/)).toBeInTheDocument();
  },
};

/**
 * The saved value comes back through the query cache, so a story with a fixed prop could never
 * show a save landing. This one reads the experiment back out of the cache, which also makes it
 * a real test of the PATCH round trip.
 */
function DescriptionFromCache() {
  const { data } = useExperiment(EXPERIMENT.id);
  return data ? <ExperimentDescriptionPanel experiment={data} /> : null;
}

/**
 * Leaving the editor saves what was typed.
 *
 * **Asserted on the wire, not on the rendered text.** Both are true, but they sit at different
 * ends of the same chain: Tiptap edit → blur → PATCH through MSW → response → cache write →
 * re-render. Waiting on the last link made the deadline, rather than the behaviour, the thing
 * that could fail — this project runs its stories many at a time in one browser, and the tail of
 * that chain under contention is not a property of the panel. The request carrying the typed
 * value *is* the claim the story's name makes, so that is what it waits for.
 *
 * The cache round trip still happens — `DescriptionFromCache` is what makes it a real one — it
 * is simply not what the assertion hangs on.
 */
export const SavesOnBlur: Story = {
  parameters: { msw: { handlers: writeSpyHandlers } },
  render: () => <DescriptionFromCache />,
  play: async ({ canvasElement }) => {
    writtenDescriptions.length = 0;
    const canvas = within(canvasElement);
    const editor = await canvas.findByRole('textbox');

    await userEvent.click(editor);
    await userEvent.keyboard('{Control>}a{/Control}Rewritten by the story.');
    // Focus has to land somewhere outside the whole widget, toolbar included.
    await userEvent.click(document.body);

    // Tiptap's own serialisation: the typed line comes back wrapped in the paragraph it edits.
    await waitFor(() => expect(writtenDescriptions).toEqual([{ description: '<p>Rewritten by the story.</p>' }]));
  },
};

/**
 * **Regression guard.** Tiptap rewrites stored HTML into its own canonical form as it loads —
 * plain text gains a `<p>`, a `<div>` becomes a `<p>`, newlines between tags are dropped — and
 * that reaches the panel through `onChange` exactly like a keystroke. Comparing the draft against
 * the *stored* string therefore reported a change before the user had touched anything, and every
 * blur PATCHed a value nobody edited. The baseline is snapshotted on focus instead.
 *
 * Plain text is the case most likely to be in real data; the `<div>` and pretty-printed variants
 * fail and pass together with it, since one mechanism covers all three.
 */
export const NoRequestWhenUnchanged: Story = {
  args: { experiment: makeExperimentDetails({ description: 'Acetylation of salicylic acid.' }) },
  parameters: { msw: { handlers: writeSpyHandlers } },
  play: async ({ canvasElement }) => {
    writtenDescriptions.length = 0;
    const canvas = within(canvasElement);
    const editor = await canvas.findByRole('textbox');

    // Tiptap has already normalised it — the DOM shows a paragraph the stored string never had.
    await expect(editor.innerHTML).toBe('<p>Acetylation of salicylic acid.</p>');

    await userEvent.click(editor);
    await userEvent.click(document.body);

    // Long enough for a PATCH to have been made, had one been made.
    await new Promise((resolve) => setTimeout(resolve, 400));
    await expect(writtenDescriptions).toEqual([]);
  },
};

/**
 * The save held open, so the in-flight state is reachable: the editor freezes and the spinner
 * appears. The default handler answers instantly and `SavingOverlay` shows nothing for a save
 * that fast, which is why this story needs its own slow one.
 */
export const Saving: Story = {
  parameters: { msw: { handlers: slowExperimentWriteHandlers } },
  render: () => <DescriptionFromCache />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const editor = await canvas.findByRole('textbox');

    await userEvent.click(editor);
    await userEvent.keyboard('{Control>}a{/Control}Held open.');
    await userEvent.click(document.body);

    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Saving…'));
    // Frozen two ways over: the overlay inerts the region, and Tiptap itself stops editing.
    await expect(editor.closest('[inert]')).not.toBeNull();
    await expect(editor).toHaveAttribute('contenteditable', 'false');
  },
};
