import { Component, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuditLogComponent } from '@core/components/common/audit-log/audit-log.component';
import { Observable } from 'rxjs';
import { Revision } from '@core/types/entities/revision.i';
import { ApiService } from '@core/services/api.service';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';

@Component({
  selector: 'eln-experiment-versions',
  standalone: true,
  imports: [CommonModule, AuditLogComponent],
  template: `
    <div class="w-full p-6 py-12">
      @if (experimentId()) {
        <eln-audit-log [entityId]="experimentId()" [loader]="load.bind(this)" />
      }
    </div>
  `,
})
export class ExperimentVersionsComponent {
  api = inject(ApiService);
  experimentDetailService = inject(ExperimentDetailService);

  experimentId = input.required<UUID>();

  load(experimentId: string): Observable<Revision[]> {
    return this.api.request('get', `experiments/${experimentId}/revisions`);
  }
}
