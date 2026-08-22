import { createFileRoute } from '@tanstack/react-router';

import { RequirePermission } from '@/components/auth/require-permission';

export const Route = createFileRoute('/_auth/dictionaries')({
  component: () => (
    <RequirePermission permission="MANAGE_DICTIONARIES">
      <h1 className="text-[16px]/6 font-semibold">Dictionaries — coming soon</h1>
    </RequirePermission>
  ),
});
