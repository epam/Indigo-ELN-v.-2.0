import { Avatar as AvatarPrimitive } from '@base-ui/react/avatar';
import { cva, type VariantProps } from 'class-variance-authority';

import { cn } from '@/lib/utils';

const avatarVariants = cva(
  'inline-flex shrink-0 items-center justify-center overflow-hidden rounded-full bg-blue-100 font-semibold text-blue-600 select-none',
  {
    variants: {
      size: {
        sm: 'size-6 text-[10px]/4',
        md: 'size-8 text-[12px]/5',
      },
    },
    defaultVariants: {
      size: 'sm',
    },
  },
);

/** The ELN API exposes no photo URL, so the initials fallback is the usual path. */
function initialsOf(displayName: string): string {
  return displayName
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('');
}

type AvatarProps = AvatarPrimitive.Root.Props &
  VariantProps<typeof avatarVariants> & {
    displayName: string;
    src?: string;
  };

function Avatar({ className, size = 'sm', displayName, src, ...props }: AvatarProps) {
  return (
    <AvatarPrimitive.Root
      data-slot="avatar"
      title={displayName}
      className={cn(avatarVariants({ size, className }))}
      {...props}
    >
      {src && <AvatarPrimitive.Image src={src} alt={displayName} className="size-full object-cover" />}
      <AvatarPrimitive.Fallback>{initialsOf(displayName)}</AvatarPrimitive.Fallback>
    </AvatarPrimitive.Root>
  );
}

export { Avatar, avatarVariants, initialsOf };
