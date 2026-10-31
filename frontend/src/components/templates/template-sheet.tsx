import { Skeleton } from '@/components/ui/skeleton';
import { Dialog, DialogContent } from '@/components/ui/dialog';
import { useTemplate } from '@/lib/api/templates';
import { describeError } from '@/lib/toast';
import { formatDate } from '@/lib/utils.ts';

import type { Template, TemplateComponent, TemplateTab } from '@/lib/types/templates.ts';
import { COMPONENT_LABELS, STOICHIOMETRY_PART_LABELS, STOICHIOMETRY_PARTS } from '@/lib/types/templates.ts';

function Section({ label, children }: { label: string; children: string }) {
  return (
    <div className="flex flex-col gap-2">
      <h3 className="font-semibold text-neutral-800">{label}</h3>
      <p className="text-neutral-1000">{children}</p>
    </div>
  );
}

/** A component by its label; the stoichiometry table also lists the blocks it was built with. */
function ComponentItem({ component }: { component: TemplateComponent }) {
  if (component.type !== 'stoichiometryTable') return <li>{COMPONENT_LABELS[component.type]}</li>;
  const parts = STOICHIOMETRY_PARTS.filter((part) => component[part]).map((part) => STOICHIOMETRY_PART_LABELS[part]);
  return (
    <li>
      {COMPONENT_LABELS.stoichiometryTable}
      <span className="text-neutral-700"> — {parts.join(', ')}</span>
    </li>
  );
}

function TabCard({ tab }: { tab: TemplateTab }) {
  return (
    <section className="flex flex-col gap-2 rounded-6 border border-neutral-300 p-4">
      <h3 className="text-[14px]/6 font-semibold text-neutral-1000">{tab.name}</h3>
      <ul className="list-disc pl-5 text-[14px]/6 text-neutral-1000">
        {tab.components.map((component) => (
          <ComponentItem key={component.type} component={component} />
        ))}
      </ul>
    </section>
  );
}

/**
 * One template, opened over the list — the `DictionarySheet` frame, mounted unconditionally with
 * `open` driving it so it can animate out.
 *
 * Read-only: the structure cannot change after creation (`PATCH /templates/{id}` takes the name
 * alone), and renaming is not part of this screen yet. The tabs come from the detail query, the
 * same cache entry an experiment built on the template reads.
 */
export function TemplateSheet({ template, onClose }: { template: Template | undefined; onClose: () => void }) {
  const details = useTemplate(template?.id);

  return (
    <Dialog open={template !== undefined} onOpenChange={(open) => !open && onClose()}>
      <DialogContent title="Template" side="right">
        {template && (
          <>
            <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
              <h2 className="border-b border-neutral-300 pb-3 text-[16px]/6 font-semibold">About Template</h2>
              <div className="flex flex-col gap-6 text-[14px]/6">
                <Section label="Template Name">{template.name}</Section>
                <Section label="Created">{`${formatDate(template.createdAt)} by ${template.createdBy.displayName}`}</Section>
                <Section label="Last Edited">{`${formatDate(template.modifiedAt)} by ${template.modifiedBy.displayName}`}</Section>
              </div>
            </section>

            <section aria-label="Structure" className="flex flex-col gap-3 rounded-6 bg-card p-4 shadow-card">
              <h2 className="border-b border-neutral-300 pb-3 text-[16px]/6 font-semibold">Tabs</h2>
              {details.isPending ? (
                <div aria-busy="true" className="flex flex-col gap-3">
                  <span className="sr-only">Loading tabs…</span>
                  <Skeleton className="h-20 w-full" />
                  <Skeleton className="h-20 w-full" />
                </div>
              ) : details.error ? (
                <p className="text-[14px]/6 text-destructive">Could not load tabs: {describeError(details.error)[0]}</p>
              ) : (
                // By position: two tabs may share a name.
                details.data.templateTabs.map((tab, index) => <TabCard key={index} tab={tab} />)
              )}
            </section>
          </>
        )}
      </DialogContent>
    </Dialog>
  );
}
