import { Search } from 'lucide-react';
import { useState } from 'react';

import logoUrl from '@/assets/indigo-logo.svg';
import { UserMenu } from '@/components/layout/user-menu';
import { GlobalSearchPanel } from '@/components/search/global-search-panel';
import { Button } from '@/components/ui/button';

export function AppHeader() {
  const [searchOpen, setSearchOpen] = useState(false);
  // Owned here rather than in the sheet: this box and the sheet's quick-search box are
  // the same search, so opening the sheet carries the term across with nothing to copy.
  const [query, setQuery] = useState('');

  return (
    <header className="flex h-18 shrink-0 items-center justify-between gap-6 bg-blue-600 px-6">
      <img src={logoUrl} alt="Indigo ELN" width={128} height={38} className="h-9.5 w-32" />

      <label className="flex h-10 w-full max-w-xl items-center gap-2 rounded-full border border-white/20 bg-white/10 px-4">
        <Search aria-hidden className="size-5 shrink-0 text-white/70" />
        <input
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') setSearchOpen(true);
          }}
          placeholder="Search"
          aria-label="Search everything"
          className="w-full bg-transparent text-[14px]/6 text-white outline-none placeholder:text-white/60"
        />
      </label>

      <div className="flex items-center gap-6">
        <Button
          variant="ghost"
          size="icon"
          aria-label="Search"
          onClick={() => setSearchOpen(true)}
          className="text-white hover:bg-white/10 hover:text-white"
        >
          <Search className="size-6" />
        </Button>
        <UserMenu />
      </div>

      <GlobalSearchPanel open={searchOpen} onOpenChange={setSearchOpen} query={query} onQueryChange={setQuery} />
    </header>
  );
}
