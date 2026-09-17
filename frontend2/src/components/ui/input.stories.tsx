import { Input } from '@/components/ui/input';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'UI/Input',
  component: Input,
  args: { placeholder: 'Project Name', 'aria-label': 'Project Name' },
  decorators: [
    (Story) => (
      <div className="w-[420px]">
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof Input>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
export const WithValue: Story = { args: { defaultValue: 'Kinase Inhibitor Screening' } };
export const Disabled: Story = { args: { disabled: true } };
