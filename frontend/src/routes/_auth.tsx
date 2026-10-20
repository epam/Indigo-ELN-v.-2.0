import { createFileRoute, Outlet, redirect } from '@tanstack/react-router';
import { AppShell } from '@/components/layout/app-shell';
import { auth } from '@/lib/auth';

export const Route = createFileRoute('/_auth')({
  beforeLoad: async ({ location }) => {
    if (await auth.getAccessToken()) return;
    // Keycloak redirects to its own login page and never hands control back here. Cognito
    // answers false, because its credential form is the /login route.
    if (await auth.requireLogin(location.href)) return;
    throw redirect({ to: '/login', search: { redirect: location.href } });
  },
  component: () => (
    <AppShell>
      <Outlet />
    </AppShell>
  ),
});
