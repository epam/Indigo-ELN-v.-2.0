import { MoreHorizontal, SquarePen } from 'lucide-react';
import { useState } from 'react';

import { RenameProjectDialog } from '@/components/projects/rename-project-dialog';
import { Button } from '@/components/ui/button';
import { Menu, MenuContent, MenuItem, MenuTrigger } from '@/components/ui/menu';
import { useProject } from '@/lib/api/projects';
import { cn } from '@/lib/utils';

import type { Project } from '@/lib/types/projects.ts';

/**
 * The "…" on a project card or row, per the design: Rename, and — once the backend has a
 * `DELETE /projects` — Delete beside it.
 *
 * **Whether Rename is allowed is asked of the project, on demand.** The list's `ProjectDTO`
 * carries no `currentPermissions`, and the right is per project (a global role *or* the access
 * level on this one — `ACLService.ensureAccess`), so the list alone cannot answer it. Opening
 * the menu fetches the detail instead: one request, only when someone asks, and the same cache
 * entry the project page reads. Rename stays disabled until it says `EDIT_PROJECTS`.
 *
 * Rendered **beside** the card's link, not inside it: a button nested in an `<a>` is invalid,
 * and its clicks would navigate. The caller positions it over the card's header.
 */
export function ProjectActionsMenu({ project, className }: { project: Project; className?: string }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const [renameOpen, setRenameOpen] = useState(false);
  const details = useProject(project.id, { enabled: menuOpen });
  const canRename = details.data?.currentPermissions.includes('EDIT_PROJECTS') ?? false;

  return (
    <>
      <Menu open={menuOpen} onOpenChange={setMenuOpen}>
        <MenuTrigger
          render={
            <Button
              variant="ghost"
              size="icon-sm"
              aria-label={`Actions for ${project.name}`}
              title="Actions"
              className={cn('text-neutral-800', className)}
            >
              <MoreHorizontal />
            </Button>
          }
        />
        <MenuContent>
          <MenuItem disabled={!canRename} onClick={() => setRenameOpen(true)}>
            <SquarePen className="size-4 text-neutral-800" />
            Rename
          </MenuItem>
        </MenuContent>
      </Menu>
      <RenameProjectDialog open={renameOpen} onOpenChange={setRenameOpen} project={project} />
    </>
  );
}
