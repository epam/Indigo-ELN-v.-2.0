import { createFileRoute } from '@tanstack/react-router';

import { RequirePermission } from '@/components/auth/require-permission';

export const Route = createFileRoute('/_auth/templates')({
  component: () => (
    <RequirePermission permission="MANAGE_TEMPLATES">
      <h1 className="text-[16px]/6 font-semibold">Templates — coming soon</h1>
    </RequirePermission>
  ),
});
