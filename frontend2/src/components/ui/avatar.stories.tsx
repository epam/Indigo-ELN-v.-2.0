import { Avatar } from '@/components/ui/avatar';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'UI/Avatar',
  component: Avatar,
  parameters: { layout: 'centered' },
  args: { displayName: 'Administrator' },
  argTypes: { size: { control: 'inline-radio', options: ['sm', 'md'] } },
} satisfies Meta<typeof Avatar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Small: Story = { args: { size: 'sm' } };

export const Medium: Story = { args: { size: 'md' } };

/** initialsOf() takes at most two words, so a single-word name yields one letter. */
export const SingleWordName: Story = { args: { displayName: 'Indigo' } };

export const LongName: Story = { args: { displayName: 'Jean-Baptiste de la Fontaine' } };
