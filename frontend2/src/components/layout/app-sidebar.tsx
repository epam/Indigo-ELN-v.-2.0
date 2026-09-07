import { Link } from '@tanstack/react-router';
import { BookA, Briefcase, type LucideIcon, PanelLeft, PanelLeftClose, Signature } from 'lucide-react';
import { useState } from 'react';

import { StarredExperiments } from '@/components/layout/starred-experiments';
import { Button } from '@/components/ui/button';
import { useCurrentUser } from '@/lib/api/user';
import type { ApplicationPermission } from '@/lib/types/user';

type NavItem = { to: string; label: string; icon: LucideIcon; permission?: ApplicationPermission };

const NAV_ITEMS = [
  { to: '/projects', label: 'All Projects', icon: Briefcase },
  { to: '/dictionaries', label: 'Dictionaries', icon: BookA, permission: 'MANAGE_DICTIONARIES' },
  { to: '/signatures', label: 'Signatures', icon: Signature, permission: 'SIGN_EXPERIMENTS' },
] as const satisfies readonly NavItem[];

export function AppSidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const { data: user } = useCurrentUser();

  // Until currentUser resolves, only the unprotected items show.
  const items = NAV_ITEMS.filter((item) => !('permission' in item) || user?.permissions.includes(item.permission));

  // Unmounted rather than clipped to `w-0 overflow-hidden`: that hides the panel visually
  // but leaves every link in the tab order and the accessibility tree, and leaves
  // StarredExperiments refetching on each window focus for a panel nobody can see.
  if (collapsed) {
    return (
      <Button
        variant="secondary"
        size="icon-lg"
        aria-label="Expand sidebar"
        className="size-10 rounded-md border border-neutral-300"
        onClick={() => setCollapsed(false)}
      >
        <PanelLeft className="size-5" />
      </Button>
    );
  }

  return (
    <aside className="flex w-75.25 flex-col justify-between gap-4 rounded-6 border border-neutral-200 bg-card px-3 pt-3 pb-5 shadow-card">
      <div className="flex flex-col gap-3">
        <Button
          variant="secondary"
          size="icon-lg"
          aria-label="Collapse sidebar"
          className="size-10 rounded-md border border-neutral-300"
          onClick={() => setCollapsed(true)}
        >
          <PanelLeftClose className="size-5" />
        </Button>

        <div className="h-px bg-neutral-300" />

        <nav className="flex flex-col gap-3">
          {items.map(({ to, label, icon: Icon }) => (
            <Link
              key={to}
              to={to}
              activeProps={{ className: 'bg-blue-10' }}
              className="flex h-11 items-center gap-2 rounded-md p-3 text-[16px]/6 font-semibold text-neutral-1000"
            >
              <Icon className="size-5" />
              {label}
            </Link>
          ))}
        </nav>

        <StarredExperiments />
      </div>
    </aside>
  );
}
