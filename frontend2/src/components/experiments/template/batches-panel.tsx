import { TemplatePlaceholder } from '@/components/experiments/template/template-placeholder';

/**
 * The `batches` template component. It brings its own card and heading rather than being wrapped
 * — `COMPONENT_TITLES` maps it to `null` — matching indigo-frontend, where the summary table is
 * one of the two components rendered outside the collapsible wrapper.
 */
export function BatchesPanel() {
  return (
    <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
      <h2 className="text-[16px]/6 font-semibold text-neutral-1000">Product Batch Summary</h2>
      {/* TODO(product-batch-summary): the batch table plus its detail panel. */}
      <TemplatePlaceholder>The product batch summary goes here.</TemplatePlaceholder>
    </section>
  );
}
