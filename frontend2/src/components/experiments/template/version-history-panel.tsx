import { TemplatePlaceholder } from '@/components/experiments/template/template-placeholder';

/** The `versionHistory` template component. Uncollapsible, for the reason given in `BatchesPanel`. */
export function VersionHistoryPanel() {
  return (
    <section className="flex flex-col gap-4 rounded-6 bg-card p-4 shadow-card">
      <h2 className="text-[16px]/6 font-semibold text-neutral-1000">Version History</h2>
      {/* TODO(version-history): GET /experiments/{id}/revisions for the log, and
          GET /experiments/{id}/revisions/{n}/diff — which answers text/html — for one entry. */}
      <TemplatePlaceholder>The revision log goes here.</TemplatePlaceholder>
    </section>
  );
}
