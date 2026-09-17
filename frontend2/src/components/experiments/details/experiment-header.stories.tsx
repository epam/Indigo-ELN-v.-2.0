import { expect, screen, userEvent } from 'storybook/test';

import { ExperimentHeader } from '@/components/experiments/details/experiment-header';
import { makeExperimentDetails, makeTemplateDetails } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const TABS = makeTemplateDetails().templateTabs;

const meta = {
  title: 'Experiments/Details/ExperimentHeader',
  component: ExperimentHeader,
  args: {
    experimentId: '22222222-2222-2222-2222-222222222222',
    experiment: makeExperimentDetails(),
    tabs: TABS,
    activeTab: 'experiment-info',
  },
} satisfies Meta<typeof ExperimentHeader>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

/**
 * Before either query resolves: the trail keeps its bare labels and the strip shows placeholder
 * pills, so the header is the same height either way and the body below never jumps.
 */
export const Loading: Story = { args: { experiment: undefined, tabs: undefined } };

/**
 * The experiment has loaded but the template has not — the strip is still pending while the
 * trail, the status and the team are already real.
 */
export const TemplatePending: Story = { args: { tabs: undefined } };

/** A signed experiment, so the badge's other hue is reachable from a story. */
export const Signed: Story = {
  args: { experiment: makeExperimentDetails({ status: 'SIGNED', marked: false }) },
};

/** The trail is the first thing to give way, so a long project name clips rather than wraps. */
export const LongProjectName: Story = {
  args: {
    experiment: makeExperimentDetails({
      projectName: 'Kinase Inhibitor Screening — Series 4 Follow-up and Selectivity Profiling',
    }),
  },
};

/** Clicking the avatar stack opens Team as the same right-hand sheet Global Search uses. */
export const OpensTeamSheet: Story = {
  play: async () => {
    await userEvent.click(await screen.findByRole('button', { name: 'Team' }));

    // Portalled, so `screen` rather than `within(canvasElement)`.
    await expect(await screen.findByRole('heading', { name: 'Team' })).toBeInTheDocument();
    await expect(screen.getByText('Sofia Rossi')).toBeInTheDocument();

    // MANAGE_EXPERIMENT_ACCESS is on the fixture, so the sheet is the editable one.
    await expect(screen.getByRole('button', { name: 'Add Member' })).toBeInTheDocument();
  },
};

/**
 * Without MANAGE_EXPERIMENT_ACCESS the sheet is a plain list, and the header's Add button is not
 * rendered at all rather than opening a sheet that cannot add anyone.
 */
export const ReadOnlyTeam: Story = {
  args: { experiment: makeExperimentDetails({ currentPermissions: ['VIEW_EXPERIMENTS'] }) },
  play: async () => {
    await expect(screen.queryByRole('button', { name: 'Add team member' })).not.toBeInTheDocument();

    await userEvent.click(await screen.findByRole('button', { name: 'Team' }));

    await expect(await screen.findByRole('heading', { name: 'Team' })).toBeInTheDocument();
    await expect(screen.queryByRole('button', { name: 'Add Member' })).not.toBeInTheDocument();
  },
};
