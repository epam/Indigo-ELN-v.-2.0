import { http, HttpResponse } from 'msw';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { ExperimentActions } from '@/components/experiments/details/experiment-actions';
import type { ApplicationPermission } from '@/lib/types/user.ts';
import { makeExperimentDetails } from '@/mocks/fixtures';
import { handlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

/** Everything the workflow needs; the header's other permissions are irrelevant here. */
const ALLOWED: ApplicationPermission[] = ['VIEW_EXPERIMENTS', 'EDIT_EXPERIMENTS', 'SUBMIT_EXPERIMENTS'];

/**
 * A collaborator at `AccessLevel.EDIT`: can change the experiment's content, cannot move it
 * through the workflow. The pair the two gates exist to tell apart.
 */
const EDITOR_ONLY: ApplicationPermission[] = ['VIEW_EXPERIMENTS', 'EDIT_EXPERIMENTS'];

/** Records the transitions that actually reached the wire, so a story can assert path and query. */
const sent: string[] = [];

const spyHandlers = [
  http.post('/api/eln/experiments/:id/workflow/:action', ({ request, params }) => {
    const { search } = new URL(request.url);
    sent.push(`${params.action as string}${search}`);
    return HttpResponse.json(makeExperimentDetails());
  }),
  ...handlers,
];

const meta = {
  title: 'Experiments/Details/ExperimentActions',
  component: ExperimentActions,
  args: { experiment: makeExperimentDetails({ currentPermissions: ALLOWED }) },
  parameters: { msw: { handlers: spyHandlers } },
  decorators: [
    (Story) => (
      <div className="flex items-center gap-2">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof ExperimentActions>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Asserts the row against the status matrix — both what is there and what is not. */
async function expectRow(canvasElement: HTMLElement, present: string[]) {
  const canvas = within(canvasElement);
  const absent = ['Complete', 'Complete and Sign', 'Cancel', 'Submit', 'Reopen'].filter(
    (label) => !present.includes(label),
  );

  for (const label of present) {
    await expect(canvas.getByRole('button', { name: label })).toBeInTheDocument();
  }
  for (const label of absent) {
    await expect(canvas.queryByRole('button', { name: label })).not.toBeInTheDocument();
  }
  // Print is on every row: `printReport` needs no permission beyond seeing the page.
  await expect(canvas.getByRole('button', { name: 'Print Report' })).toBeEnabled();
}

/** The three transitions an editable experiment offers. `REOPEN` renders identically. */
export const Open: Story = {
  play: ({ canvasElement }) => expectRow(canvasElement, ['Complete', 'Complete and Sign', 'Cancel']),
};

export const Completed: Story = {
  args: { experiment: makeExperimentDetails({ status: 'COMPLETED', currentPermissions: ALLOWED }) },
  play: ({ canvasElement }) => expectRow(canvasElement, ['Submit', 'Reopen']),
};

/**
 * A rejected experiment is resubmitted by the **same** button — `SubmitExperimentHandler` allows
 * `COMPLETED` and `REJECTED` alike, so there is no separate Resubmit action.
 */
export const Rejected: Story = {
  args: { experiment: makeExperimentDetails({ status: 'REJECTED', currentPermissions: ALLOWED }) },
  play: ({ canvasElement }) => expectRow(canvasElement, ['Submit', 'Reopen']),
};

/**
 * Out for signature: nothing to offer anybody. `ReopenExperimentHandler` excludes `SIGNING` and
 * `SIGNED`, so the Reopen indigo-frontend shows here would only earn a 400.
 */
export const Signing: Story = {
  args: { experiment: makeExperimentDetails({ status: 'SIGNING', currentPermissions: ALLOWED }) },
  play: ({ canvasElement }) => expectRow(canvasElement, []),
};

/**
 * The permission gate, which is the *other* gate: the buttons the status allows are still rendered,
 * disabled and saying why, rather than vanishing. Print stays live — it needs no permission.
 */
export const WithoutPermission: Story = {
  args: { experiment: makeExperimentDetails({ currentPermissions: EDITOR_ONLY }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    for (const label of ['Complete', 'Complete and Sign', 'Cancel']) {
      const button = canvas.getByRole('button', { name: label });
      await expect(button).toBeDisabled();
      await expect(button).toHaveAccessibleDescription(/permission/i);
    }
    await expect(canvas.getByRole('button', { name: 'Print Report' })).toBeEnabled();
  },
};

/** Complete has no dialog in front of it — Reopen is its undo, so nothing is lost by firing. */
export const CompletesImmediately: Story = {
  play: async ({ canvasElement }) => {
    sent.length = 0;
    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Complete' }));

    await waitFor(() => expect(sent).toEqual(['complete']));
    await expect(await screen.findByText('Experiment marked as completed')).toBeInTheDocument();
  },
};

/**
 * Cancel is the one transition that asks first: it throws work away, and its own label makes the
 * dialog's buttons say what they do rather than the default Save/Cancel pair.
 */
export const CancelAsksFirst: Story = {
  play: async ({ canvasElement }) => {
    sent.length = 0;
    const canvas = within(canvasElement);

    // Portalled, so `screen` rather than the canvas.
    await userEvent.click(canvas.getByRole('button', { name: 'Cancel' }));
    await expect(await screen.findByRole('button', { name: 'Cancel Experiment' })).toBeInTheDocument();

    // Backing out sends nothing.
    await userEvent.click(screen.getByRole('button', { name: 'Keep Experiment' }));
    await waitFor(() => expect(screen.queryByRole('button', { name: 'Cancel Experiment' })).not.toBeInTheDocument());
    await expect(sent).toEqual([]);

    await userEvent.click(canvas.getByRole('button', { name: 'Cancel' }));
    await userEvent.click(await screen.findByRole('button', { name: 'Cancel Experiment' }));
    await waitFor(() => expect(sent).toEqual(['cancel']));
  },
};

/**
 * Submit stops at the template picker, and the id it chooses rides along as a query param —
 * the one thing `POST /workflow/submit` needs beyond the experiment.
 */
export const SubmitPicksTemplate: Story = {
  args: { experiment: makeExperimentDetails({ status: 'COMPLETED', currentPermissions: ALLOWED }) },
  play: async ({ canvasElement }) => {
    sent.length = 0;

    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Submit' }));

    // Required field: Sign cannot be pressed until a template has actually been chosen.
    const sign = await screen.findByRole('button', { name: 'Sign' });
    await expect(sign).toBeDisabled();

    await userEvent.click(screen.getByLabelText(/^Signature Template/));
    await userEvent.click(await screen.findByRole('option', { name: 'Author and Witness' }));
    await waitFor(() => expect(sign).toBeEnabled());

    await userEvent.click(sign);
    await waitFor(() => expect(sent).toEqual(['submit?signatureTemplateId=77777777-7777-7777-7777-777777777771']));
  },
};

/** Complete and Sign goes through the same picker, to the endpoint that does both. */
export const CompleteAndSign: Story = {
  play: async ({ canvasElement }) => {
    sent.length = 0;

    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Complete and Sign' }));
    await userEvent.click(await screen.findByLabelText(/^Signature Template/));
    await userEvent.click(await screen.findByRole('option', { name: 'Author only' }));
    await userEvent.click(screen.getByRole('button', { name: 'Sign' }));

    await waitFor(() =>
      expect(sent).toEqual(['completeAndSubmit?signatureTemplateId=77777777-7777-7777-7777-777777777772']),
    );
  },
};
