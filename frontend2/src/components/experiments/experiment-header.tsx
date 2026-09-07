import { Link } from '@tanstack/react-router';
import { UserPlus } from 'lucide-react';
import { useState } from 'react';

import { AvatarStack } from '@/components/common/avatar-stack';
import { TeamSheet } from '@/components/common/team-sheet';
import { ExperimentActions, UndoRedoButtons } from '@/components/experiments/experiment-actions';
import { tabSlugs } from '@/components/experiments/experiment-template';
import { StarButton } from '@/components/experiments/star-button';
import { useExperimentSaving, useUpdateExperimentAccess } from '@/lib/api/experiments';
import { Breadcrumbs } from '@/components/layout/breadcrumbs';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import { EXPERIMENT_STATUS_DISPLAY } from '@/lib/types/experiments.ts';
import { cn } from '@/lib/utils';
import type { TemplateTab } from '@/lib/types/templates.ts';

// Split across three constants rather than merged, matching ProjectHeader — see the comment
// there. The sets have to stay disjoint, since twMerge never runs over the join.
const TAB_CLASS = 'border-b-2 px-2 pb-2 text-[14px]/6';
const TAB_ACTIVE_CLASS = 'border-blue-400 font-semibold text-blue-400';
const TAB_INACTIVE_CLASS = 'border-transparent text-neutral-800';

/**
 * How many faces the stack shows before the `+N` chip. The detail endpoint returns the whole ACL
 * rather than the capped list and count the collection DTOs carry, so the cap is applied here —
 * the full length still goes in as `aclCount`, which is what makes the overflow honest.
 */
const AVATARS_SHOWN = 3;

/**
 * The card above the experiment's body: trail, status, team, actions, and the tab strip the
 * template dictates.
 *
 * `experiment` and `tabs` arrive undefined while their two queries resolve — the strip shows
 * placeholder pills at the tab height, so the header never changes height as the data lands.
 *
 * The trail names both ancestors from `projectId`/`projectName`/`notebookId`/`notebookName` on
 * the experiment payload: the URL is flat (`/experiments/{id}`) and carries neither, so there is
 * nothing else to read and no second request to make.
 */
export function ExperimentHeader({
  experimentId,
  experiment,
  tabs,
  activeTab,
}: {
  experimentId: string;
  experiment: ExperimentDetails | undefined;
  tabs: TemplateTab[] | undefined;
  /** The `?tab=` slug in force, already resolved against the template by the route. */
  activeTab: string | undefined;
}) {
  const [teamOpen, setTeamOpen] = useState(false);
  const saving = useExperimentSaving(experimentId);
  // Two instances of one mutation — see TeamCard for why they are not shared.
  const addMembers = useUpdateExperimentAccess(experimentId);
  const changeLevel = useUpdateExperimentAccess(experimentId);
  const slugs = tabs ? tabSlugs(tabs) : [];
  const canManage = experiment?.currentPermissions.includes('MANAGE_EXPERIMENT_ACCESS') ?? false;

  return (
    <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
      <div className="flex items-center gap-4">
        <Breadcrumbs
          items={[
            { label: 'All Projects', link: { to: '/projects' } },
            {
              label: experiment ? `Project: ${experiment.projectName}` : 'Project:',
              link: experiment ? { to: '/projects/$id', params: { id: experiment.projectId } } : undefined,
            },
            {
              label: experiment ? `Notebook: ${experiment.notebookName}` : 'Notebook:',
              link: experiment ? { to: '/notebooks/$id', params: { id: experiment.notebookId } } : undefined,
            },
            { label: experiment ? `Experiment: ${experiment.name}` : 'Experiment:' },
          ]}
          className="min-w-0 flex-1"
        />

        {experiment && (
          <>
            <Badge variant={experiment.status} className="w-auto shrink-0">
              {EXPERIMENT_STATUS_DISPLAY[experiment.status]}
            </Badge>
            {/* TODO(star): useToggleMark has to patch the experimentDetails cache entry first,
                or the star reverts as the invalidated lists come home. */}
            <StarButton experimentId={experimentId} marked={experiment.marked} disabled />

            {/*
              A button here rather than inside AvatarStack: the stack also renders inside the
              <Link> of every experiment card, where a nested button would be invalid markup and
              would swallow the navigation.
            */}
            <button
              type="button"
              aria-label="Team"
              onClick={() => setTeamOpen(true)}
              className="cursor-pointer rounded-full outline-none focus-visible:ring-3 focus-visible:ring-ring/50"
            >
              <AvatarStack acl={experiment.acl.slice(0, AVATARS_SHOWN)} aclCount={experiment.acl.length} />
            </button>
            {/* The same sheet, opened at the part of it that adds someone — so it is only here
                when that part exists. Without the permission the sheet is a plain list, and a
                button promising otherwise would be a dead end. */}
            {canManage && (
              <Button variant="outline" size="icon-lg" aria-label="Add team member" onClick={() => setTeamOpen(true)}>
                <UserPlus />
              </Button>
            )}
          </>
        )}

        <ExperimentActions />
      </div>

      <div className="-mb-4 flex items-end justify-between gap-4 border-b border-neutral-300">
        <nav className="flex gap-2">
          {tabs
            ? tabs.map((tab, index) => (
                <Link
                  key={slugs[index]}
                  to="/experiments/$id"
                  params={{ id: experimentId }}
                  search={{ tab: slugs[index] }}
                  replace
                  className={cn(TAB_CLASS, slugs[index] === activeTab ? TAB_ACTIVE_CLASS : TAB_INACTIVE_CLASS)}
                >
                  {tab.name}
                </Link>
              ))
            : // Placeholders at the tab height, so the strip does not grow as the template lands.
              [0, 1, 2].map((index) => (
                <span key={index} className={cn(TAB_CLASS, TAB_INACTIVE_CLASS)}>
                  <Skeleton className="my-1 h-4 w-20" />
                </span>
              ))}
        </nav>

        {/* A skeleton at the icon-lg height while the experiment loads, so the strip does not
            grow when it lands — the same reason the tabs above have placeholder pills. */}
        <div className="pb-2">
          {experiment ? <UndoRedoButtons experiment={experiment} saving={saving} /> : <Skeleton className="h-9 w-20" />}
        </div>
      </div>

      {experiment && (
        <TeamSheet
          open={teamOpen}
          onOpenChange={setTeamOpen}
          acl={experiment.acl}
          canManage={canManage}
          addMembers={addMembers}
          changeLevel={changeLevel}
        />
      )}
    </section>
  );
}
