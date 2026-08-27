import { createFileRoute } from '@tanstack/react-router';

import { RequirePermission } from '@/components/auth/require-permission';

export const Route = createFileRoute('/_auth/signatures')({
  component: () => (
    <RequirePermission permission="SIGN_EXPERIMENTS">
      <h1 className="text-[16px]/6 font-semibold">Signatures — coming soon</h1>
    </RequirePermission>
  ),
});
