import { Fragment } from 'react';

import { CollapsibleCard } from '@/components/common/collapsible-card';
import { COMPONENT_TITLES } from '@/lib/types/templates.ts';
import { AttachmentsPanel } from '@/components/experiments/template/attachments-panel';
import { BatchesPanel } from '@/components/experiments/template/batches-panel';
import { ExperimentDescriptionPanel } from '@/components/experiments/template/experiment-description-panel';
import { ExperimentDetailsPanel } from '@/components/experiments/template/experiment-details-panel';
import { StoichiometryPanel } from '@/components/experiments/template/stoichiometry-panel';
import { VersionHistoryPanel } from '@/components/experiments/template/version-history-panel';

import type { ExperimentDetails } from '@/lib/types/experiments.ts';
import type { TemplateComponent, TemplateTab } from '@/lib/types/templates.ts';

/**
 * One template component, in the card its title asks for.
 *
 * The `switch` is exhaustive over the union rather than a lookup table, which is what makes a
 * seventh component type added to `templates.ts` a compile error here — indigo-frontend's
 * equivalent `@switch` would simply have rendered nothing.
 *
 * Which components get a card and which frame themselves is `COMPONENT_TITLES`' business alone;
 * a `null` there is the whole signal.
 */
function TemplateComponentView({
  component,
  experiment,
  titleId,
}: {
  component: TemplateComponent;
  experiment: ExperimentDetails;
  titleId: string;
}) {
  switch (component.type) {
    case 'experimentDetails':
      return <ExperimentDetailsPanel experiment={experiment} />;
    case 'experimentDescription':
      return <ExperimentDescriptionPanel experiment={experiment} labelledBy={titleId} />;
    case 'attachments':
      return <AttachmentsPanel experiment={experiment} />;
    case 'stoichiometryTable':
      return <StoichiometryPanel component={component} experiment={experiment} />;
    case 'batches':
      return <BatchesPanel experiment={experiment} />;
    case 'versionHistory':
      return <VersionHistoryPanel />;
  }
}

/** Everything one template tab declares, in the order the template declares it. */
export function ExperimentTemplateTab({ tab, experiment }: { tab: TemplateTab; experiment: ExperimentDetails }) {
  return (
    <div className="flex flex-col gap-4">
      {tab.components.map((component, index) => {
        const title = COMPONENT_TITLES[component.type];
        // Type and index together: a tab is not supposed to declare the same component twice, but
        // nothing in the template data stops it, and duplicate keys would be the worse failure.
        const key = `${component.type}-${index}`;
        const titleId = `template-component-${key}`;
        const view = <TemplateComponentView component={component} experiment={experiment} titleId={titleId} />;

        return title === null ? (
          <Fragment key={key}>{view}</Fragment>
        ) : (
          <CollapsibleCard key={key} title={title} titleId={titleId}>
            {view}
          </CollapsibleCard>
        );
      })}
    </div>
  );
}
