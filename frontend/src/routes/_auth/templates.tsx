import { createFileRoute } from '@tanstack/react-router';
import { Plus } from 'lucide-react';
import { useMemo, useState } from 'react';

import { RequirePermission } from '@/components/auth/require-permission';
import { TemplateFormDialog } from '@/components/templates/template-form-dialog';
import { TemplateList } from '@/components/templates/template-list';
import { TemplateSheet } from '@/components/templates/template-sheet';
import { Button } from '@/components/ui/button';
import { useTemplates } from '@/lib/api/templates';
import { usePageTitle } from '@/lib/hooks/use-page-title';
import { z } from '@/lib/zod';

/** The open template's id, so the sheet is linkable and Back closes it — as on Dictionaries. */
const searchSchema = z.object({
  template: z.string().optional(),
});

export const Route = createFileRoute('/_auth/templates')({
  validateSearch: searchSchema,
  component: () => (
    <RequirePermission permission="MANAGE_TEMPLATES">
      <TemplatesPage />
    </RequirePermission>
  ),
});

/**
 * Template management: the list, a sheet for the one opened, and Add Template. Every write on
 * `/templates` is gated on the global `MANAGE_TEMPLATES` (`TemplateService`), so the whole page is.
 */
function TemplatesPage() {
  usePageTitle('Templates');
  const { template: selectedId } = Route.useSearch();
  const navigate = Route.useNavigate();
  const [addOpen, setAddOpen] = useState(false);

  // The Add Experiment picker's query — one page big enough to be the whole list.
  const query = useTemplates(true);
  const templates = query.data?.items;
  // Resolved from the list rather than fetched again; a stale `?template=` leaves the sheet shut.
  const selected = templates?.find((item) => item.id === selectedId);
  const existingNames = useMemo(() => (templates ?? []).map((template) => template.name), [templates]);

  // Pushed, not replaced, so Back undoes opening a sheet — see DictionariesPage.
  const select = (id: string | undefined) =>
    void navigate({ search: (prev) => ({ ...prev, template: id }), resetScroll: false });

  return (
    <>
      <div className="flex items-center justify-between gap-4">
        <h1 className="text-[16px]/6 font-semibold">Templates</h1>
        <Button size="lg" className="rounded-md" onClick={() => setAddOpen(true)}>
          <Plus />
          Add Template
        </Button>
      </div>
      <TemplateList query={query} selectedId={selected?.id} onSelect={(item) => select(item.id)} />
      <TemplateSheet template={selected} onClose={() => select(undefined)} />
      <TemplateFormDialog
        open={addOpen}
        onOpenChange={setAddOpen}
        existingNames={existingNames}
        // The new template's sheet opens on it, so what was just built is shown back at once.
        onCreated={(created) => select(created.id)}
      />
    </>
  );
}
