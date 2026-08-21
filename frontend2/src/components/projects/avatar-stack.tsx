import { Avatar } from '@/components/ui/avatar';
import type { ACLEntry } from '@/lib/types/common.ts';

const VISIBLE = 3;

export function AvatarStack({ acl, aclCount }: { acl: ACLEntry[]; aclCount: number }) {
  const shown = acl.slice(0, VISIBLE);
  const overflow = aclCount - shown.length;

  return (
    <div className="flex items-center">
      {shown.map((entry, index) => (
        <Avatar
          key={entry.username}
          displayName={entry.displayName}
          className={index > 0 ? '-ml-2 ring-2 ring-card' : undefined}
        />
      ))}
      {overflow > 0 && (
        <span className="-ml-2 flex h-6 min-w-6 items-center justify-center rounded-full bg-blue-400 px-1 text-[10px]/4 font-bold text-white ring-2 ring-card">
          +{overflow}
        </span>
      )}
    </div>
  );
}
