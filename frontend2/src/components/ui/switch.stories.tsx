import { useState } from 'react';

import { Switch } from '@/components/ui/switch';

import type { Meta, StoryObj } from '@storybook/react-vite';

const meta = {
  title: 'UI/Switch',
  component: Switch,
  parameters: { layout: 'centered' },
} satisfies Meta<typeof Switch>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Off: Story = { args: { checked: false, 'aria-label': 'Created by me' } };

export const On: Story = { args: { checked: true, 'aria-label': 'Created by me' } };

export const Disabled: Story = { args: { checked: true, disabled: true, 'aria-label': 'Created by me' } };

function ControlledSwitch() {
  const [checked, setChecked] = useState(false);
  return <Switch checked={checked} onCheckedChange={setChecked} aria-label="Created by me" />;
}

export const Interactive: Story = {
  render: () => <ControlledSwitch />,
};
