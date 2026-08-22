import { Search } from 'lucide-react';

import logoUrl from '@/assets/indigo-logo.svg';
import { UserMenu } from '@/components/layout/user-menu';
import { Button } from '@/components/ui/button';

export function AppHeader() {
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
        <UserMenu />
      </div>
    </header>
  );
}
