import { useRouter } from '@tanstack/react-router';
import { signOut } from 'aws-amplify/auth';
import { ChevronDown, LogOut } from 'lucide-react';

import { Avatar } from '@/components/ui/avatar';
import { Menu, MenuContent, MenuItem, MenuTrigger } from '@/components/ui/menu';
import { useCurrentUser } from '@/lib/api/user';
import { clearPersistedCache } from '@/lib/query-client';
import { notifyError } from '@/lib/toast';

export function UserMenu() {
  const { data } = useCurrentUser();
  const router = useRouter();
  const displayName = data?.displayName ?? '';

  async function logout() {
    try {
      await signOut();
    } catch (error) {
      // The session is still live, so stay put rather than stranding the user on /login.
      notifyError(error);
      return;
    }
    await clearPersistedCache();
    await router.navigate({ to: '/login' });
  }

  // Nothing to click until currentUser resolves.
  if (!displayName) return null;

  return (
    <Menu>
      {/* The avatar carries the name as a `title`, which would otherwise be read out
          ahead of the label; the visible name beside it is the button's whole point. */}
      <MenuTrigger className="flex cursor-pointer items-center gap-2.5 rounded-2 px-2 py-2 text-white outline-none hover:bg-white/10 focus-visible:ring-3 focus-visible:ring-white/50">
        <Avatar displayName={displayName} aria-hidden />
        <span className="text-[16px]/7">{displayName}</span>
        <ChevronDown className="size-4" />
      </MenuTrigger>
      <MenuContent>
        <MenuItem onClick={logout}>
          <LogOut className="size-4" />
          Log Out
        </MenuItem>
      </MenuContent>
    </Menu>
  );
}
