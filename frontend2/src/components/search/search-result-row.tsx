import { Link } from '@tanstack/react-router';
import { Briefcase, FlaskConical, NotebookText } from 'lucide-react';
import { createContext, use } from 'react';

import { ReactionScheme } from '@/components/search/reaction-scheme';
import { Badge } from '@/components/ui/badge';
import { EXPERIMENT_STATUS_DISPLAY } from '@/lib/types/experiments.ts';
import type { GlobalSearchResult, SearchEntityType } from '@/lib/types/search.ts';
import { REACTION_ROLE_DISPLAY } from '@/lib/types/search.ts';

/**
 * Lets a row close the sheet it was clicked in. Passed by context rather than as a prop
 * because `Collection` renders items as `ComponentType<{ item: T }>` and hands them nothing
 * else; threading it through a memoised component identity instead would remount the whole
 * list whenever the callback changed.
 */
const SelectResultContext = createContext<() => void>(() => {});

const ENTITY_LABEL: Record<SearchEntityType, string> = {
  PROJECT: 'Project',
  NOTEBOOK: 'Notebook',
  EXPERIMENT: 'Experiment',
};

const ENTITY_ICON: Record<SearchEntityType, typeof Briefcase> = {
  PROJECT: Briefcase,
  NOTEBOOK: NotebookText,
  EXPERIMENT: FlaskConical,
};

/** The card's classes, shared by the row and its skeleton so neither reflows into the other. */
const CARD_CLASS = 'flex flex-col gap-3 rounded-6 border border-neutral-300 bg-card px-4 pt-3 pb-4';
const COLUMNS_CLASS = 'grid min-w-0 flex-1 gap-x-4 gap-y-2 grid-cols-[repeat(auto-fit,minmax(140px,1fr))]';

function Column({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="flex min-w-0 flex-col">
      <dt className="truncate text-[14px]/6 text-neutral-800">{label}</dt>
      <dd className="flex items-center gap-2 overflow-hidden text-[14px]/6">{children}</dd>
    </div>
  );
}

function Count({ children }: { children: React.ReactNode }) {
  return <span className="text-[16px]/6 font-semibold">{children}</span>;
}

function Columns({ item }: { item: GlobalSearchResult }) {
  return (
    <dl className={COLUMNS_CLASS}>
      {item.type === 'EXPERIMENT' && (
        // Always shown for an experiment: the subject is a property of the experiment, so
        // its absence is worth seeing. Role is a property of the *match* and only exists
        // for a molecule search, so it is left out entirely rather than shown empty.
        <Column label="Subject">
          <span className="truncate">{item.title ?? '—'}</span>
        </Column>
      )}
      {item.reactionRoles && item.reactionRoles.length > 0 && (
        <Column label="Role">
          <span className="truncate">{item.reactionRoles.map((role) => REACTION_ROLE_DISPLAY[role]).join(', ')}</span>
        </Column>
      )}
      {item.type === 'PROJECT' && (
        <Column label="Notebooks">
          <Count>{item.notebookCount ?? 0}</Count>
        </Column>
      )}
      {item.type !== 'EXPERIMENT' && (
        <Column label="Experiments">
          <Count>{item.experimentCount ?? 0}</Count>
        </Column>
      )}
      <Column label="Creator">
        <span className="truncate">{item.createdBy.displayName}</span>
      </Column>
    </dl>
  );
}

function Body({ item }: { item: GlobalSearchResult }) {
  const Icon = ENTITY_ICON[item.type];

  return (
    <>
      <header className="flex min-h-7 items-center gap-4">
        <Icon className="size-5 shrink-0 text-neutral-800" />
        <h3 className="flex-1 truncate text-[14px]/5">
          <span className="text-neutral-800">{ENTITY_LABEL[item.type]} </span>
          <span className="font-semibold">{item.name}</span>
        </h3>
        {item.experimentStatus && (
          // Badge is a fixed w-[59px] and truncates; the longer statuses need the room.
          <Badge variant={item.experimentStatus} className="w-auto shrink-0">
            {EXPERIMENT_STATUS_DISPLAY[item.experimentStatus]}
          </Badge>
        )}
      </header>

      <div className="flex items-start gap-4">
        {item.type === 'EXPERIMENT' && <ReactionScheme experimentId={item.id} revision={item.revision} />}
        <Columns item={item} />
      </div>
    </>
  );
}

/**
 * One search hit. Which fields appear depends on the entity type: only experiments have a
 * scheme, a status, a subject and a matched role; only projects count notebooks.
 *
 * Projects and experiments link to their detail pages and close the sheet on the way.
 * Notebooks have no route in this app yet, so they render as a plain card rather than a
 * link that goes nowhere.
 */
function SearchResultRow({ item }: { item: GlobalSearchResult }) {
  const onSelect = use(SelectResultContext);

  if (item.type === 'NOTEBOOK') {
    return (
      <article className={CARD_CLASS}>
        <Body item={item} />
      </article>
    );
  }

  return (
    <Link
      to={item.type === 'PROJECT' ? '/projects/$id' : '/experiments/$id'}
      params={{ id: item.id }}
      onClick={onSelect}
      className={`${CARD_CLASS} cursor-pointer`}
    >
      <Body item={item} />
    </Link>
  );
}

export { CARD_CLASS, COLUMNS_CLASS, SearchResultRow, SelectResultContext };
