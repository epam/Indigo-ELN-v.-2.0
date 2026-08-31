import {expect, userEvent, waitFor, within} from 'storybook/test';

import {StarButton} from '@/components/experiments/star-button';

import type {Meta, StoryObj} from '@storybook/react-vite';

const meta = {
  title: 'Experiments/StarButton',
  component: StarButton,
  args: { experimentId: '22222222-2222-2222-2222-222222222222', marked: false },
} satisfies Meta<typeof StarButton>;

export default meta;
type Story = StoryObj<typeof meta>;

export const NotStarred: Story = {};

export const Starred: Story = { args: { marked: true } };

/**
 * `marked` comes from the list the button sits in, so a story with it as a fixed prop only
 * pins that the POST is made and the button returns from its pending state.
 */
export const Toggling: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    const button = canvas.getByRole('button', { name: 'Add to starred' });
    await userEvent.click(button);
    await waitFor(() => expect(button).not.toHaveAttribute('aria-busy', 'true'));
  },
};
