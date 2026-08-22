import { createFileRoute } from '@tanstack/react-router';

import { RequirePermission } from '@/components/auth/require-permission';

export const Route = createFileRoute('/_auth/users')({
  component: () => (
    <RequirePermission permission="MANAGE_USERS">
      <h1 className="text-[16px]/6 font-semibold">Users — coming soon</h1>
    </RequirePermission>
  ),
});
