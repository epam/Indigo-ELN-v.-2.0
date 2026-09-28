import { StarredExperiments } from '@/components/layout/starred-experiments';
import { emptyHandlers, errorHandlers, loadingHandlers } from '@/mocks/handlers';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'Layout/StarredExperiments',
  component: StarredExperiments,
  decorators: [
    (Story) => (
      <div className="w-[280px]">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof StarredExperiments>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const Loading: Story = {
  parameters: { msw: { handlers: loadingHandlers } },
};

export const Empty: Story = {
  parameters: { msw: { handlers: emptyHandlers } },
};

export const Error: Story = {
  parameters: { msw: { handlers: errorHandlers } },
};
