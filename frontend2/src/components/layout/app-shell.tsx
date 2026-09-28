import type { ReactNode } from 'react';

import { AppHeader } from '@/components/layout/app-header';
import { AppSidebar } from '@/components/layout/app-sidebar';

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-svh flex-col bg-neutral-200 text-foreground">
      <AppHeader />
      <div className="flex flex-1 items-start gap-4 p-4">
        <AppSidebar />
        <main className="flex min-w-0 flex-1 flex-col gap-4">{children}</main>
      </div>
    </div>
  );
}
