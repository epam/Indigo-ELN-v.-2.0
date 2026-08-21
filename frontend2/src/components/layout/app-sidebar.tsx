import { Link, useRouter } from '@tanstack/react-router';
import { signOut } from 'aws-amplify/auth';
import { BookA, Briefcase, LayoutTemplate, LogOut, PanelLeft, PanelLeftClose, Users } from 'lucide-react';
import { useState } from 'react';

import { StarredExperiments } from '@/components/layout/starred-experiments';
import { Button } from '@/components/ui/button';

const NAV_ITEMS = [
  { to: '/projects', label: 'All Projects', icon: Briefcase },
  { to: '/templates', label: 'Templates', icon: LayoutTemplate },
  { to: '/dictionaries', label: 'Dictionaries', icon: BookA },
  { to: '/users', label: 'Users', icon: Users },
] as const;

export function AppSidebar() {
  const router = useRouter();
  const [collapsed, setCollapsed] = useState(false);

  const handleSignOut = async () => {
    await signOut();
    router.history.replace('/login');
  };

  return (
    <div className="flex items-start gap-2">
      <aside
        className={
          collapsed
            ? 'w-0 overflow-hidden'
            : 'flex w-[301px] flex-col justify-between gap-4 rounded-6 border border-neutral-200 bg-card px-3 pt-3 pb-5 shadow-card'
        }
      >
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
            {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
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

        <Button
          variant="ghost"
          className="h-11 justify-start gap-2 rounded-md p-3 text-[16px]/6 font-semibold"
          onClick={handleSignOut}
        >
          <LogOut className="size-5" />
          Log Out
        </Button>
      </aside>

      {collapsed && (
        <Button
          variant="secondary"
          size="icon-lg"
          aria-label="Expand sidebar"
          className="size-10 rounded-md border border-neutral-300"
          onClick={() => setCollapsed(false)}
        >
          <PanelLeft className="size-5" />
        </Button>
      )}
    </div>
  );
}
