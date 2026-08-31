import { ExperimentTemplateTab } from '@/components/experiments/experiment-template-tab';
import { makeExperimentDetails, makeTemplateDetails } from '@/mocks/fixtures';

import type { Meta, StoryObj } from '@storybook/react-vite';

const TABS = makeTemplateDetails().templateTabs;
const EXPERIMENT = makeExperimentDetails();

const meta = {
  title: 'Experiments/ExperimentTemplateTab',
  component: ExperimentTemplateTab,
  args: { tab: TABS[0], experiment: EXPERIMENT },
} satisfies Meta<typeof ExperimentTemplateTab>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Details in a card, stoichiometry framing itself — the two halves of the wrapping rule. */
export const ExperimentInfo: Story = {};

/** Two carded components in a row. */
export const Attachments: Story = { args: { tab: TABS[1] } };

/** `batches` brings its own heading, so nothing wraps it. */
export const Summary: Story = { args: { tab: TABS[2] } };

/** Likewise `versionHistory`. */
export const PreviousVersions: Story = { args: { tab: TABS[3] } };

/** A tab a template left empty renders nothing rather than an empty card. */
export const EmptyTab: Story = { args: { tab: { name: 'Empty', components: [] } } };
