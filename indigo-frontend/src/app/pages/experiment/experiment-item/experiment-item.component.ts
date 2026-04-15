import { BadgeComponent } from '@/core/components/common/badge/badge.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ExperimentDetail } from '@/core/types/entities/experiments/experiment-detail.i';
import { CommonModule } from '@angular/common';
import { Component, DestroyRef, Input, effect, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltip } from '@angular/material/tooltip';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { InitialsPipe } from '@/core/pipes/avatars.pipe';
import { getExperimentStatusBadgeVariant } from '@/core/utils/experiment-status.util';
import { SvgIconComponent } from '@/core/components/common/svg-icon/svg-icon.component';
import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError } from 'rxjs';

@Component({
  selector: 'eln-experiment-item',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    BadgeComponent,
    NormalizeLabelPipe,
    InitialsPipe,
    MatTooltip,
    SvgIconComponent,
  ],
  templateUrl: './experiment-item.component.html',
})
export class ExperimentItemComponent {
  private readonly router = inject(Router);
  private readonly experimentDetailService = inject(ExperimentDetailService);
  private readonly destroyRef = inject(DestroyRef);

  @Input() experiment!: ExperimentDetail;
  @Input() variant: 'grid' | 'list' = 'grid';
  @Input() projectId!: string;
  @Input() notebookId!: string;

  isMarked = signal(false);

  constructor() {
    effect(() => {
      this.isMarked.set(this.experiment.marked ?? false);
    });
  }

  toggleMark(event: Event): void {
    event.stopPropagation();
    const wasMarked = this.isMarked();
    const nowMarked = !wasMarked;
    this.isMarked.set(nowMarked);

    const request$ = nowMarked
      ? this.experimentDetailService.mark(this.experiment.id)
      : this.experimentDetailService.unmark(this.experiment.id);

    request$
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        catchError(() => {
          this.isMarked.set(wasMarked);
          return [];
        }),
      )
      .subscribe();
  }

  openDetails(): void {
    if (!this.experiment || !this.projectId || !this.notebookId) return;
    const url = `/projects/${this.projectId}/notebooks/${this.notebookId}/experiments/${this.experiment.id}`;
    this.router.navigateByUrl(url);
  }

  getStatusBadgeVariant = getExperimentStatusBadgeVariant;

  get statusLabel(): string {
    return this.experiment.status;
  }
}