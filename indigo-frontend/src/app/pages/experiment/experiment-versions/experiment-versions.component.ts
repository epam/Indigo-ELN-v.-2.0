import { Component, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuditLogComponent } from '@core/components/common/audit-log/audit-log.component';
import { Observable } from 'rxjs';
import { RevisionSummary } from '@core/types/entities/revision.i';
import { ApiService } from '@core/services/api.service';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';
import { map } from 'rxjs/operators';

@Component({
  selector: 'eln-experiment-versions',
  standalone: true,
  imports: [CommonModule, AuditLogComponent],
  template: `
    <div class="w-full p-6">
      @if (experimentId()) {
        <eln-audit-log [entityId]="experimentId()" [loader]="load.bind(this)" [diffLoader]="loadDiff.bind(this)" />
      }
    </div>
  `,
})
export class ExperimentVersionsComponent {
  api = inject(ApiService);
  experimentDetailService = inject(ExperimentDetailService);

  experimentId = input.required<UUID>();

  load(experimentId: string): Observable<RevisionSummary[]> {
    return this.api.request<RevisionSummary[]>('get', `experiments/${experimentId}/revisions`).pipe(
      map((revisions) => {
        revisions.forEach((revision) => (revision.details = revision.details?.reverse()));
        return revisions.reverse();
      }),
    );
  }

  loadDiff(revision: RevisionSummary): Observable<string> {
    return this.api.request('get', `experiments/${this.experimentId()}/revisions/${revision.revision}/diff`, null, {
      responseType: 'text',
    });
  }
}
