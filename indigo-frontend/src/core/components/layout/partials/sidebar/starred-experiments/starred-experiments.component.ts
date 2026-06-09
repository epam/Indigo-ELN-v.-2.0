import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CardComponent } from '@/core/components/common/card/card.component';
import { BadgeComponent } from '@/core/components/common/badge/badge.component';
import { ApiService } from '@/core/services/api.service';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { finalize, Subject, takeUntil } from 'rxjs';
import { ExperimentStatus } from '@/core/enums/experiment-status.enum';
import { ExperimentDetail } from '@/core/types/entities/experiments/experiment-detail.i';
import { EXPERIMENT_STATUS_DECORATION_MAP, ExperimentStatusDecoration } from '@/core/utils/experiment-status.util';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';

@Component({
  selector: 'eln-starred-experiments',
  templateUrl: './starred-experiments.component.html',
  standalone: true,
  imports: [CommonModule, CardComponent, BadgeComponent, NormalizeLabelPipe],
})
export class StarredExperimentsComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private readonly service = inject(ApiService);
  private readonly experimentDetailService = inject(ExperimentDetailService);

  loading = false;
  error: string | null = null;
  experiments: ExperimentDetail[] | null = null;

  readonly statusDecorMap: Record<ExperimentStatus, ExperimentStatusDecoration> = EXPERIMENT_STATUS_DECORATION_MAP;

  ngOnInit(): void {
    this.fetchMarkedExperiments();
    this.experimentDetailService.markedChanged$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.fetchMarkedExperiments());
  }

  private fetchMarkedExperiments(): void {
    if (this.experiments == null) {
      this.loading = true;
    }
    this.service
      .request<ExperimentDetail[]>('get', 'experiments/marked')
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (resp) => (this.experiments = Array.isArray(resp) ? resp : null),
        error: () => (this.error = 'Failed to load starred experiments'),
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
