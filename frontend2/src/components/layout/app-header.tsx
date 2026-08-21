import { Search } from 'lucide-react';

import logoUrl from '@/assets/indigo-logo.svg';
import { Avatar } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { useCurrentUser } from '@/lib/api/user';

export function AppHeader() {
  const { data } = useCurrentUser();
  const displayName = data?.displayName ?? '';

  return (
    <header className="flex h-[72px] shrink-0 items-center justify-between bg-blue-600 px-6">
      <img src={logoUrl} alt="Indigo ELN" width={128} height={38} className="h-[38px] w-[128px]" />
      <div className="flex items-center gap-6">
        <Button
          variant="ghost"
          size="icon"
          aria-label="Search"
          className="text-white hover:bg-white/10 hover:text-white"
        >
          <Search className="size-6" />
        </Button>
        <div className="flex items-center gap-2.5 rounded-2 py-2">
          {displayName && <Avatar displayName={displayName} />}
          <span className="text-[16px]/7 text-white">{displayName}</span>
        </div>
      </div>
    </header>
  );
}
