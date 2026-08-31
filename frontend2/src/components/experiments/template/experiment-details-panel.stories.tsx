import { http, HttpResponse } from 'msw';
import { expect, screen, userEvent, waitFor, within } from 'storybook/test';

import { ExperimentDetailsPanel } from '@/components/experiments/template/experiment-details-panel';
import { useExperiment } from '@/lib/api/experiments';
import { DICTIONARIES, makeExperimentDetails } from '@/mocks/fixtures';
import { handlers, slowExperimentWriteHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const EXPERIMENT = makeExperimentDetails();

/** Records what actually reached the wire, so a story can assert what was — and was not — sent. */
const patches: unknown[] = [];

const patchSpyHandlers = [
  http.patch('/api/eln/experiments/:id', async ({ request, params }) => {
    patches.push(await request.json());
    return HttpResponse.json(makeExperimentDetails({ id: String(params.id) }));
  }),
  ...handlers,
];

const meta = {
  title: 'Experiments/Template/ExperimentDetailsPanel',
  component: ExperimentDetailsPanel,
  args: { experiment: EXPERIMENT },
} satisfies Meta<typeof ExperimentDetailsPanel>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Two columns of fields, and the experiment's own provenance in a third, read-only one. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByLabelText('Experiment Title')).toHaveValue(EXPERIMENT.title ?? '');
    await expect(canvas.getByText(EXPERIMENT.name)).toBeInTheDocument();
    await expect(canvas.getByText(EXPERIMENT.batchCreator.displayName)).toBeInTheDocument();
  },
};

/** Nothing filled in yet — every control shows its placeholder. */
export const Empty: Story = {
  args: {
    experiment: makeExperimentDetails({
      title: undefined,
      therapeuticArea: undefined,
      projectCode: undefined,
      literature: undefined,
      linkedExperiments: [],
      continuedFrom: [],
      continuedTo: [],
    }),
  },
};

/** A reader who cannot edit gets every control, filled in and inert. */
export const ReadOnly: Story = {
  args: { experiment: makeExperimentDetails({ currentPermissions: ['VIEW_EXPERIMENTS'] }) },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByLabelText('Experiment Title')).toBeDisabled();
    await expect(canvas.getByLabelText('Therapeutic Area')).toBeDisabled();
    // Base UI marks a disabled button `aria-disabled` rather than using the native attribute.
    await expect(canvas.getByRole('button', { name: /^Remove / })).toHaveAttribute('aria-disabled', 'true');
    // The read-only column is unaffected — it was never a control.
    await expect(canvas.getByText(EXPERIMENT.name)).toBeInTheDocument();
  },
};

/**
 * The saved value comes back through the query cache, so a story with a fixed prop could never
 * show a save landing. These read the experiment back out of the cache instead.
 */
function DetailsFromCache() {
  const { data } = useExperiment(EXPERIMENT.id);
  return data ? <ExperimentDetailsPanel experiment={data} /> : null;
}

/** Picking a therapeutic area saves at once, sending the whole `{id, name}` ref. */
export const PicksTherapeuticArea: Story = {
  parameters: { msw: { handlers: patchSpyHandlers } },
  render: () => <DetailsFromCache />,
  play: async ({ canvasElement }) => {
    patches.length = 0;
    const canvas = within(canvasElement);

    await userEvent.click(await canvas.findByLabelText('Therapeutic Area'));
    // The popup is portalled, so it is reached with `screen`, not the canvas.
    const option = DICTIONARIES.THERAPEUTIC_AREA![3];
    await userEvent.click(await screen.findByRole('option', { name: option.name }));

    await waitFor(() => expect(patches).toEqual([{ therapeuticArea: option }]));
  },
};

/**
 * **Regression guard**, the same class the description panel carries: opening a field and leaving
 * it without changing anything must send nothing.
 */
export const NoRequestWhenUnchanged: Story = {
  parameters: { msw: { handlers: patchSpyHandlers } },
  render: () => <DetailsFromCache />,
  play: async ({ canvasElement }) => {
    patches.length = 0;
    const canvas = within(canvasElement);

    const title = await canvas.findByLabelText('Experiment Title');
    await userEvent.click(title);
    await userEvent.click(document.body);

    // …and the picker, opened and dismissed with Escape.
    await userEvent.click(canvas.getByLabelText('Therapeutic Area'));
    await userEvent.keyboard('{Escape}');
    await userEvent.click(document.body);

    await new Promise((resolve) => setTimeout(resolve, 400));
    await expect(patches).toEqual([]);
  },
};

/** The save held open, so one field freezes while its neighbours stay live. */
export const Saving: Story = {
  parameters: { msw: { handlers: slowExperimentWriteHandlers } },
  render: () => <DetailsFromCache />,
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);

    const title = await canvas.findByLabelText('Experiment Title');
    await userEvent.click(title);
    await userEvent.type(title, ' revised');
    await userEvent.click(document.body);

    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Saving…'));
    await expect(title).toBeDisabled();
    // Only that field: the neighbour is untouched.
    await expect(canvas.getByLabelText('Therapeutic Area')).not.toBeDisabled();
  },
};
