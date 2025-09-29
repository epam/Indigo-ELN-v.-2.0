import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CardComponent } from '@/core/components/common/card/card.component';
import { BadgeComponent } from '@/core/components/common/badge/badge.component';
import { ApiService } from '@/core/services/api.service';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { catchError, of, Subject } from 'rxjs';
import { ExperimentStatus } from '@/core/enums/experiment-status.enum';
import { Experiment } from '@/core/types/entities/experiment.i';
import { EXPERIMENT_STATUS_DECORATION_MAP, ExperimentStatusDecoration } from '@/core/utils/experiment-status.util';

@Component({
  selector: 'eln-starred-experiments',
  templateUrl: './starred-experiments.component.html',
  standalone: true,
  imports: [CommonModule, CardComponent, BadgeComponent, NormalizeLabelPipe],
})
export class StarredExperimentsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private service = inject(ApiService);

  loading = false;
  error: string | null = null;
  experiments: Experiment[] = [];

  readonly statusDecorMap: Record<ExperimentStatus, ExperimentStatusDecoration> = EXPERIMENT_STATUS_DECORATION_MAP;

  ngOnInit(): void {
    this.fetchMarkedExperiments();
  }

  private fetchMarkedExperiments(): void {
    this.loading = true;
    this.service
      .request<Experiment[]>('get', 'experiments/marked')
      .pipe(
        catchError((err) => {
          console.error('Failed to load marked experiments:', err);
          this.error = 'Failed to load starred experiments';
          this.loading = false;
          return of(null);
        }),
      )
      .subscribe({
        next: (resp) => {
          this.experiments = Array.isArray(resp) ? resp : [];
        },
        error: (err) => {
          console.error('Failed to load marked experiments:', err);
          this.error = 'Failed to load starred experiments';
        },
        complete: () => {
          this.loading = false;
        },
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
