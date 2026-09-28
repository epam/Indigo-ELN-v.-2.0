import { useState } from 'react';
import { expect, userEvent, within } from 'storybook/test';

import { Field } from '@/components/ui/field';
import { PasswordInput } from '@/components/ui/password-input';

import type { Meta, StoryObj } from '@storybook/react-vite';

/** Controlled, so every story drives it from local state rather than from an arg. */
function Harness({ initial = '', clearable = true }: { initial?: string; clearable?: boolean }) {
  const [value, setValue] = useState(initial);

  return (
    <div className="w-[420px]">
      <Field id="password" label="Password">
        <PasswordInput
          id="password"
          label="Password"
          placeholder="Enter your Password"
          autoComplete="current-password"
          clearable={clearable}
          value={value}
          onChange={setValue}
        />
      </Field>
    </div>
  );
}

const meta = {
  title: 'UI/PasswordInput',
  component: Harness,
} satisfies Meta<typeof Harness>;

export default meta;
type Story = StoryObj<typeof meta>;

/** Empty: the reveal button is there, the clear one has nothing to clear. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByRole('button', { name: 'Show Password' })).toBeVisible();
    await expect(canvas.queryByRole('button', { name: 'Clear Password' })).toBeNull();
  },
};

/** The clear button appears with the first keystroke — and nothing shifts when it does. */
export const Filled: Story = {
  args: { initial: 'hunter2hunter2' },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByLabelText('Password')).toHaveAttribute('type', 'password');
    await userEvent.click(canvas.getByRole('button', { name: 'Clear Password' }));
    await expect(canvas.getByLabelText('Password')).toHaveValue('');
  },
};

/** Revealed: the toggle swaps the input's type and renames itself. */
export const Revealed: Story = {
  args: { initial: 'hunter2hunter2' },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByRole('button', { name: 'Show Password' }));
    await expect(canvas.getByLabelText('Password')).toHaveAttribute('type', 'text');
    await expect(canvas.getByRole('button', { name: 'Hide Password' })).toBeVisible();
  },
};

/** `clearable={false}` leaves only the reveal toggle, however much is typed. */
export const NotClearable: Story = {
  args: { initial: 'hunter2hunter2', clearable: false },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await expect(canvas.queryByRole('button', { name: 'Clear Password' })).toBeNull();
    await expect(canvas.getByRole('button', { name: 'Show Password' })).toBeVisible();
  },
};
