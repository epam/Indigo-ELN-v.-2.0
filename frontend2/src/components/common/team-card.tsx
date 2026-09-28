import { Check, ChevronDown, Copy, UserPlus } from 'lucide-react';
import { useState } from 'react';

import { MultiCombobox } from '@/components/ui/combobox';
import { Avatar } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { Menu, MenuContent, MenuItem, MenuTrigger } from '@/components/ui/menu';
import { useUserSuggestions } from '@/lib/api/user';
import { cn } from '@/lib/utils';
import type { UseMutationResult } from '@tanstack/react-query';
import type { AccessForm, AccessLevel, ACLEntry, UserRef } from '@/lib/types/common.ts';
import { ACL_LEVEL_LABELS, ELIGIBLE_ACL_LEVELS, isImmutableLevel } from '@/lib/types/common.ts';

/** What a newly added member gets. Anything more is a deliberate step up in the level menu. */
const DEFAULT_LEVEL: AccessLevel = 'VIEW';

/**
 * Every entity's `POST /access` has the same shape, so one mutation type covers projects,
 * notebooks and experiments alike.
 */
export type AccessMutation = UseMutationResult<ACLEntry[], Error, AccessForm[]>;

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

/**
 * The members of a project, notebook or experiment, with the controls to add one and to
 * re-level an existing one — the card's contents without its frame, so the Team sheet can show
 * the same list under its own title instead of a card nested in a card.
 *
 * The two mutations arrive as props rather than being made here: they post to different
 * endpoints per entity, and each caller's `onSuccess` patches its own cached detail. They stay
 * two instances of the same mutation, not one — both post to /access, but each owns a spinner
 * in a different place, and a shared `isPending` could not say which asked for it.
 *
 * They are optional because `canManage: false` reaches neither: the add row is not rendered and
 * every level renders as locked text. A read-only surface should not have to invent a mutation
 * it will never fire.
 */
export function TeamMembers({
  acl,
  canManage,
  addMembers,
  changeLevel,
}: {
  acl: ACLEntry[];
  canManage: boolean;
  addMembers?: AccessMutation;
  changeLevel?: AccessMutation;
}) {
  const [inputValue, setInputValue] = useState('');
  const [selected, setSelected] = useState<UserRef[]>([]);
  const suggestions = useUserSuggestions(inputValue);

  // Already-members are dropped from the list rather than shown and rejected on submit.
  const members = new Set(acl.map((entry) => entry.username));
  const items = (suggestions.data ?? []).filter((user) => !members.has(user.username));

  // A level change is always one entry, so its `variables` name the row to spin.
  const updatingUsername = changeLevel?.isPending ? changeLevel.variables?.[0]?.username : undefined;

  return (
    <>
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
              /*
                What the control actually accepts, which is not what the design's placeholder
                said. `allowCustomValues` is off — a member has to be an existing user — so typed
                text is never committed, comma is deliberately not a commit key (see
                `MultiCombobox`), and `users/suggest` matches a name or username prefix rather
                than an address. "Emails, comma separated" promised all three.
              */
              placeholder="Search by name or username"
              emptyMessage="No matching users"
              loading={inputValue !== '' && suggestions.isPending}
              error={suggestions.isError}
            />
          </div>
          <Button
            variant="outline"
            disabled={selected.length === 0}
            loading={addMembers?.isPending}
            onClick={() =>
              addMembers?.mutate(
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
        {acl.map((member) => (
          <MemberRow
            key={member.username}
            member={member}
            canManage={canManage}
            updating={updatingUsername === member.username}
            onLevelChange={(level) => changeLevel?.mutate([{ username: member.username, level }])}
          />
        ))}
      </ul>
    </>
  );
}

/**
 * `TeamMembers` in the card frame the Info tabs put it in: a heading, a count, and the shadowed
 * white surface every other card on those pages uses.
 */
export function TeamCard(props: {
  acl: ACLEntry[];
  canManage: boolean;
  addMembers: AccessMutation;
  changeLevel: AccessMutation;
}) {
  return (
    <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
      <div className="flex items-center gap-2 border-b border-neutral-300 pb-3">
        <h2 className="text-[16px]/6 font-semibold">Team</h2>
        <span className="flex h-6 min-w-6 items-center justify-center rounded-full bg-neutral-200 px-2 text-[12px]/5 font-semibold text-neutral-800">
          {props.acl.length}
        </span>
      </div>
      <TeamMembers {...props} />
    </section>
  );
}
