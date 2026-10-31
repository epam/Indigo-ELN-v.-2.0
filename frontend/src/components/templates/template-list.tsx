import { TemplateRow } from '@/components/templates/template-row';
import { TemplateRowSkeleton } from '@/components/templates/template-row-skeleton';
import { describeError } from '@/lib/toast';

import type { UseQueryResult } from '@tanstack/react-query';
import type { Page } from '@/lib/types/common.ts';
import type { Template } from '@/lib/types/templates.ts';

/** Roughly a screenful, so the first load lands without the document resizing under the reader. */
const FIRST_LOAD_SKELETONS = 4;

const LIST_CLASS = 'flex flex-col gap-3';

/**
 * The templates, as stacked rows — `DictionaryList`'s three branches over `useTemplates`, which
 * asks for one page big enough to be the whole list, so there is no second page to load.
 *
 * The query is passed in: the route needs the same list to resolve its search param and to tell
 * Add Template which names are taken.
 */
export function TemplateList({
  query,
  selectedId,
  onSelect,
}: {
  query: UseQueryResult<Page<Template>, Error>;
  selectedId: string | undefined;
  onSelect: (template: Template) => void;
}) {
  const { data, error, isPending } = query;

  if (isPending) {
    return (
      <div aria-busy="true" className={LIST_CLASS}>
        <span className="sr-only">Loading templates…</span>
        {Array.from({ length: FIRST_LOAD_SKELETONS }, (_, index) => (
          <TemplateRowSkeleton key={index} />
        ))}
      </div>
    );
  }

  if (error) {
    return <p className="text-[14px]/6 text-destructive">Could not load templates: {describeError(error)[0]}</p>;
  }

  if (data.items.length === 0) {
    return <p className="text-[14px]/6 text-neutral-700">No templates yet.</p>;
  }

  return (
    <div className={LIST_CLASS}>
      {data.items.map((template) => (
        <TemplateRow
          key={template.id}
          template={template}
          selected={template.id === selectedId}
          onSelect={() => onSelect(template)}
        />
      ))}
    </div>
  );
}
