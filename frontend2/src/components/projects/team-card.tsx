import {Check, ChevronDown, Copy, UserPlus} from 'lucide-react';
import {useState} from 'react';

import {MultiCombobox} from '@/components/ui/combobox';
import {Avatar} from '@/components/ui/avatar';
import {Button} from '@/components/ui/button';
import {Menu, MenuContent, MenuItem, MenuTrigger} from '@/components/ui/menu';
import {useUpdateProjectAccess} from '@/lib/api/projects';
import {useUserSuggestions} from '@/lib/api/user';
import {cn} from '@/lib/utils';
import type {AccessLevel, ACLEntry, UserRef} from '@/lib/types/common.ts';
import {ACL_LEVEL_LABELS, ELIGIBLE_ACL_LEVELS, isImmutableLevel} from '@/lib/types/common.ts';
import type {ProjectDetails} from '@/lib/types/projects.ts';

/** What a newly added member gets. Anything more is a deliberate step up in the level menu. */
const DEFAULT_LEVEL: AccessLevel = 'VIEW';

/**
 * Avatar, name, username, copy, level. The `minmax(0,…)` floors are what let the two text cells
 * truncate — an `auto` track floors at min-content, which under `white-space: nowrap` equals the
 * full string, so the row would overflow instead of clipping.
 *
 * **Every track is row-independent on purpose.** Content-sized tracks (`auto`, `max-content`) are
 * resolved per row, so a column holding "Admin" in one row and "Limited View" in the next comes
 * out staggered — which is the whole defect this layout exists to fix. Hence the fixed 7.5rem for
 * the level (it clears the longest label plus its chevron) and fr units, not content, for the text.
 *
 * The name gets 1.5fr against the username's 1fr because it carries the `(Inherited)` marker too,
 * and an even split left it clipping names while the username column sat half empty.
 */
const ROW_COLUMNS = 'grid-cols-[auto_minmax(0,1.5fr)_minmax(0,1fr)_auto_7.5rem]';

function CopyUsernameButton({ username }: { username: string }) {
  const [copied, setCopied] = useState(false);

  return (
    <Button
      variant="ghost"
      size="icon-xs"
      aria-label={`Copy ${username}`}
      onClick={() => {
        void navigator.clipboard.writeText(username);
        setCopied(true);
        window.setTimeout(() => setCopied(false), 1500);
      }}
    >
      {copied ? <Check className="text-green-200" /> : <Copy className="text-neutral-700" />}
    </Button>
  );
}

function MemberRow({
  member,
  canManage,
  updating,
  onLevelChange,
}: {
  member: ACLEntry;
  canManage: boolean;
  updating: boolean;
  onLevelChange: (level: AccessLevel) => void;
}) {
  // AUTHOR is the creator's own entry; the backend rejects any change to it.
  const locked = isImmutableLevel(member.level) || !canManage;

  return (
    <li className={cn('grid items-center gap-x-2 py-2 text-[14px]/6', ROW_COLUMNS)}>
      <Avatar displayName={member.displayName} />

      <span className="flex min-w-0 items-baseline gap-1">
        <span className="truncate">{member.displayName}</span>
        {/* Outside the truncating span, so a long name clips itself rather than eating the marker. */}
        {member.inherited && <span className="shrink-0 text-[12px]/5 text-neutral-700">(Inherited)</span>}
      </span>

      {/* title, because the column is the first thing to give way and the address is the point. */}
      <span className="truncate text-neutral-700" title={member.username}>
        {member.username}
      </span>

      <CopyUsernameButton username={member.username} />

      {locked ? (
        <span className="truncate px-2.5 text-neutral-700">{ACL_LEVEL_LABELS[member.level]}</span>
      ) : (
        <Menu>
          <MenuTrigger
            render={
              // w-full + justify-between so the chevron lands on the same x in every row.
              <Button variant="ghost" size="sm" loading={updating} className="w-full justify-between text-blue-400">
                {/* Button's base is whitespace-nowrap, so an over-long label needs this to clip. */}
                <span className="truncate">{ACL_LEVEL_LABELS[member.level]}</span>
                <ChevronDown />
              </Button>
            }
          />
          <MenuContent>
            {ELIGIBLE_ACL_LEVELS.map((level) => (
              <MenuItem key={level} onClick={() => onLevelChange(level)}>
                {ACL_LEVEL_LABELS[level]}
              </MenuItem>
            ))}
          </MenuContent>
        </Menu>
      )}
    </li>
  );
}

export function TeamCard({ project }: { project: ProjectDetails }) {
  const canManage = project.currentPermissions.includes('MANAGE_PROJECT_ACCESS');
  // Two instances of the same mutation rather than one: both post to /access, but each owns a
  // spinner in a different place, and a shared `isPending` could not say which asked for it.
  const addMembers = useUpdateProjectAccess(project.id);
  const changeLevel = useUpdateProjectAccess(project.id);

  const [inputValue, setInputValue] = useState('');
  const [selected, setSelected] = useState<UserRef[]>([]);
  const suggestions = useUserSuggestions(inputValue);

  // Already-members are dropped from the list rather than shown and rejected on submit.
  const members = new Set(project.acl.map((entry) => entry.username));
  const items = (suggestions.data ?? []).filter((user) => !members.has(user.username));

  // A level change is always one entry, so its `variables` name the row to spin.
  const updatingUsername = changeLevel.isPending ? changeLevel.variables?.[0]?.username : undefined;

  return (
    <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
      <div className="flex items-center gap-2 border-b border-neutral-300 pb-3">
        <h2 className="text-[16px]/6 font-semibold">Team</h2>
        <span className="flex h-6 min-w-6 items-center justify-center rounded-full bg-neutral-200 px-2 text-[12px]/5 font-semibold text-neutral-800">
          {project.acl.length}
        </span>
      </div>

      {canManage && (
        <div className="flex items-start gap-2">
          <div className="min-w-0 flex-1">
            <MultiCombobox<UserRef>
              id="team-add-member"
              aria-label="Add team members"
              value={selected}
              onValueChange={setSelected}
              items={items}
              itemToKey={(user) => user.username}
              itemToLabel={(user) => user.displayName}
              inputValue={inputValue}
              onInputValueChange={setInputValue}
              placeholder="Emails, comma separated"
              emptyMessage="No matching users"
              loading={inputValue !== '' && suggestions.isPending}
              error={suggestions.isError}
            />
          </div>
          <Button
            variant="outline"
            disabled={selected.length === 0}
            loading={addMembers.isPending}
            onClick={() =>
              addMembers.mutate(
                selected.map((user) => ({ username: user.username, level: DEFAULT_LEVEL })),
                { onSuccess: () => setSelected([]) },
              )
            }
          >
            <UserPlus />
            Add Member
          </Button>
        </div>
      )}

      <ul className="flex flex-col divide-y divide-neutral-300">
        {project.acl.map((member) => (
          <MemberRow
            key={member.username}
            member={member}
            canManage={canManage}
            updating={updatingUsername === member.username}
            onLevelChange={(level) => changeLevel.mutate([{ username: member.username, level }])}
          />
        ))}
      </ul>
    </section>
  );
}
