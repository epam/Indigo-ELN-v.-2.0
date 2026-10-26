import { useCallback, useMemo } from 'react';

import { useCurrentUser } from '@/lib/api/user';
import { useStoredState } from '@/lib/hooks/use-stored-state';

/** A column the Column Selector offers: what it is called in the menu, and what it is stored as. */
export interface HideableColumn {
  id: string;
  header: string;
}

/** Each table keeps its own choice; the id is part of the storage key. */
export type ColumnChoiceTable = 'stoichiometryInputs' | 'stoichiometryProducts';

function isStringArray(raw: unknown): raw is string[] {
  return Array.isArray(raw) && raw.every((item) => typeof item === 'string');
}

const NOTHING_HIDDEN: string[] = [];

/**
 * Which of a table's columns the user has switched off — the stoichiometry tables' Column Selector.
 *
 * Kept per user and per table in this browser, as the Angular app did: a layout preference, not
 * experiment data, so it follows the user rather than the experiment and never touches the server.
 * The **hidden** ids are what is stored, not the visible ones, so a column added later shows up by
 * default instead of starting out hidden for everyone who ever saved a choice.
 *
 * Only the offered `columns` count: a stored id the table no longer offers is ignored, so it can
 * neither hide a fixed column nor linger in the menu.
 */
export function useHiddenColumns(table: ColumnChoiceTable, columns: readonly HideableColumn[]) {
  const userId = useCurrentUser().data?.id;
  const key = userId == null ? undefined : `indigo.${userId}.${table}.hiddenColumns`;
  const [stored, setStored] = useStoredState(key, NOTHING_HIDDEN, isStringArray);

  const hidden = useMemo(() => {
    const offered = new Set(columns.map((column) => column.id));
    return new Set(stored.filter((id) => offered.has(id)));
  }, [stored, columns]);

  const toggle = useCallback(
    (id: string, visible: boolean) => {
      const next = new Set(hidden);
      if (visible) next.delete(id);
      else next.add(id);
      setStored([...next]);
    },
    [hidden, setStored],
  );

  const reset = useCallback(() => setStored(NOTHING_HIDDEN), [setStored]);

  return { hidden, toggle, reset };
}
