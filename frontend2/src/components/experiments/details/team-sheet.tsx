import { TeamMembers } from '@/components/common/team-card';
import { Dialog, DialogContent } from '@/components/ui/dialog';

import type { AccessMutation } from '@/components/common/team-card';
import type { ACLEntry } from '@/lib/types/common.ts';

/**
 * The team as a slide-in rather than a card — what the experiment screen shows when its avatar
 * stack is clicked, since that page has no column to park a Team card in.
 *
 * It is the same sheet Global Search uses: `DialogContent side="right"`, which is why there is no
 * `Sheet` component in `ui/`. Base UI still traps focus and closes on Escape or a click outside,
 * and the backdrop stays transparent so the page behind reads as the thing this is about.
 *
 * The prop shape matches `TeamCard` so a project or notebook can swap one for the other.
 */
export function TeamSheet({
  open,
  onOpenChange,
  acl,
  canManage,
  addMembers,
  changeLevel,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  acl: ACLEntry[];
  canManage: boolean;
  addMembers?: AccessMutation;
  changeLevel?: AccessMutation;
}) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent title="Team" side="right">
        <TeamMembers acl={acl} canManage={canManage} addMembers={addMembers} changeLevel={changeLevel} />
      </DialogContent>
    </Dialog>
  );
}
