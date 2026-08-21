import { Link } from '@tanstack/react-router';
import { Plus } from 'lucide-react';

import { Button } from '@/components/ui/button';

import type { Meta, StoryObj } from '@storybook/react-vite';

const VARIANTS = ['default', 'outline', 'secondary', 'ghost', 'destructive', 'link'] as const;
const SIZES = ['xs', 'sm', 'default', 'lg'] as const;

const meta = {
  title: 'UI/Button',
  component: Button,
  parameters: { layout: 'centered' },
  argTypes: {
    variant: { control: 'select', options: VARIANTS },
    size: { control: 'select', options: SIZES },
  },
} satisfies Meta<typeof Button>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Playground: Story = {
  args: { children: 'Create project' },
};

export const Variants: Story = {
  args: { children: 'Button' },
  render: () => (
    <div className="flex flex-wrap items-center gap-3">
      {VARIANTS.map((variant) => (
        <Button key={variant} variant={variant}>
          {variant}
        </Button>
      ))}
    </div>
  ),
};

export const Sizes: Story = {
  args: { children: 'Button' },
  render: () => (
    <div className="flex items-center gap-3">
      {SIZES.map((size) => (
        <Button key={size} size={size}>
          {size}
        </Button>
      ))}
    </div>
  ),
};

export const IconOnly: Story = {
  args: { children: <Plus />, size: 'icon', 'aria-label': 'Add' },
};

export const Disabled: Story = {
  args: { children: 'Unavailable', disabled: true },
};

/**
 * Base UI's `render` prop is how a Button becomes a router Link (see __root.tsx).
 * `nativeButton={false}` is required: the rendered element is an <a>, not a <button>.
 */
export const AsLink: Story = {
  args: { children: 'All projects' },
  render: (args) => <Button {...args} nativeButton={false} render={<Link to="/projects" />} />,
};
