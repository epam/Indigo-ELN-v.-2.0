import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';
import { fetchAuthSession } from 'aws-amplify/auth';

import { AppShell } from '@/components/layout/app-shell';

export const Route = createFileRoute('/_auth')({
  beforeLoad: async ({ location }) => {
    const session = await fetchAuthSession().catch(() => null);
    if (!session?.tokens?.accessToken) {
      throw redirect({ to: '/login', search: { redirect: location.href } });
    }
  },
  component: () => (
    <AppShell>
      <Outlet />
    </AppShell>
  ),
});
