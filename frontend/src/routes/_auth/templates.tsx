import { createFileRoute } from '@tanstack/react-router';
import { Plus } from 'lucide-react';
import { useMemo, useState } from 'react';

import { RequirePermission } from '@/components/auth/require-permission';
import { STARTER_TEMPLATE_NAME } from '@/components/templates/template-form';
import { TemplateFormDialog } from '@/components/templates/template-form-dialog';
import { TemplateList } from '@/components/templates/template-list';
import { TemplateSheet } from '@/components/templates/template-sheet';
import { Button } from '@/components/ui/button';
import { useTemplate, useTemplates } from '@/lib/api/templates';
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
 * Template management: the list, a sheet for the one opened, and Add Template.
 *
 * Gated on the global `MANAGE_TEMPLATES` as a whole. Reading templates needs nothing — Add
 * Experiment lists them for everyone — but this is the screen for managing them, and every write
 * it offers is refused without that permission (`TemplateService`). Like Dictionaries behind
 * `MANAGE_DICTIONARIES`, it is an admin's page, not a reader's.
 */
function TemplatesPage() {
  usePageTitle('Templates');
  const { template: selectedId } = Route.useSearch();
  const navigate = Route.useNavigate();
  const [addOpen, setAddOpen] = useState(false);

  // One page big enough to be the whole list, shared with the Add Experiment picker's cache.
  const query = useTemplates(true);
  const templates = query.data?.items;
  // Resolved from the list rather than fetched again; a stale `?template=` leaves the sheet shut.
  const selected = templates?.find((item) => item.id === selectedId);
  const existingNames = useMemo(() => (templates ?? []).map((template) => template.name), [templates]);

  // What Add Template starts as a copy of. Fetched with the page rather than when the dialog
  // opens, so the copy is usually there already; the same cached detail the sheet reads.
  const starterId = templates?.find((template) => template.name === STARTER_TEMPLATE_NAME)?.id;
  const starter = useTemplate(starterId);

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
        starter={starter.data?.templateTabs}
        // Only while a starter exists and has not arrived; without one the dialog starts empty.
        initializing={query.isPending || (starterId !== undefined && starter.isPending)}
        // The new template's sheet opens on it, so what was just built is shown back at once.
        onCreated={(created) => select(created.id)}
      />
    </>
  );
}
