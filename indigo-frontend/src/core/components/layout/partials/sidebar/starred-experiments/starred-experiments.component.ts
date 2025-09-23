import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CardComponent } from '@/core/components/common/card/card.component';
import { BadgeComponent } from '@/core/components/common/badge/badge.component';
import { ApiService } from '@/core/services/api.service';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { catchError, of, Subject } from 'rxjs';
import { ExperimentStatus } from '@/core/enums/experiment-status.enum';
import { Experiment } from '@/core/types/entities/experiment.i';

// Shared types for decoration palette
type BadgeVariant = 'blue' | 'green' | 'yellow' | 'red' | 'grey' | 'violet';
interface StatusDecorarion { variant: BadgeVariant; dotClass: string; }

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

  readonly statusDecorMap: Record<ExperimentStatus, StatusDecorarion> = {
    [ExperimentStatus.OPEN]: { variant: 'blue', dotClass: 'bg-primary-400' },
    [ExperimentStatus.REOPEN]: { variant: 'blue', dotClass: 'bg-primary-400' },
    [ExperimentStatus.COMPLETED]: { variant: 'green', dotClass: 'bg-green-200' },
    [ExperimentStatus.SUBMITTED]: { variant: 'yellow', dotClass: 'bg-yellow-200' },
    [ExperimentStatus.SIGNING]: { variant: 'yellow', dotClass: 'bg-yellow-200' },
    [ExperimentStatus.REJECTED]: { variant: 'red', dotClass: 'bg-red-200' },
    [ExperimentStatus.SIGNED]: { variant: 'green', dotClass: 'bg-green-200' },
    [ExperimentStatus.ARCHIVED]: { variant: 'grey', dotClass: 'bg-neutral-500' },
    [ExperimentStatus.CANCELLED]: { variant: 'red', dotClass: 'bg-red-200' },
    [ExperimentStatus.WAITING_FOR_SIGNATURE]: { variant: 'violet', dotClass: 'bg-violet-200' },
  };

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
