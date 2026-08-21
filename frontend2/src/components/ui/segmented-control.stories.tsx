import { LayoutGrid, List as ListIcon } from 'lucide-react';
import { useState } from 'react';

import { SegmentedControl } from '@/components/ui/segmented-control';

import type { Meta, StoryObj } from '@storybook/react-vite';

const OPTIONS = [
  { value: 'grid' as const, label: 'Grid view', icon: <LayoutGrid className="size-5" /> },
  { value: 'list' as const, label: 'List view', icon: <ListIcon className="size-5" /> },
];

const meta = {
  title: 'UI/SegmentedControl',
  component: SegmentedControl,
  parameters: { layout: 'centered' },
} satisfies Meta<typeof SegmentedControl<'grid' | 'list'>>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Grid: Story = {
  args: { value: 'grid', options: OPTIONS, onValueChange: () => {} },
};

export const List: Story = {
  args: { value: 'list', options: OPTIONS, onValueChange: () => {} },
};

function ControlledSegmentedControl() {
  const [view, setView] = useState<'grid' | 'list'>('grid');
  return <SegmentedControl value={view} onValueChange={setView} options={OPTIONS} />;
}

export const Interactive: Story = {
  args: { value: 'grid', options: OPTIONS, onValueChange: () => {} },
  render: () => <ControlledSegmentedControl />,
};
