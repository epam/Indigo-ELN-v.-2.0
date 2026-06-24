import { BadgeComponent } from '@/core/components/common/badge/badge.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { SvgIconComponent } from '@/core/components/common/svg-icon/svg-icon.component';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { ExperimentDetail } from '@/core/types/entities/experiments/experiment-detail.i';
import { getExperimentStatusBadgeVariant } from '@/core/utils/experiment-status.util';
import { CommonModule } from '@angular/common';
import { Component, inject, Input, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router } from '@angular/router';
import { ApiImageComponent } from '@core/components/common/image/api-image.component';
import { MemberAvatarsComponent } from '@core/components/common/member-avatars/member-avatars.component';

@Component({
  selector: 'eln-experiment-item',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
    BadgeComponent,
    NormalizeLabelPipe,
    SvgIconComponent,
    ApiImageComponent,
    MemberAvatarsComponent,
  ],
  templateUrl: './experiment-item.component.html',
})
export class ExperimentItemComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly experimentDetailService = inject(ExperimentDetailService);

  @Input({ required: true })
  experiment!: ExperimentDetail;

  @Input()
  variant: 'grid' | 'list' = 'grid';

  isMarked = signal(false);

  ngOnInit(): void {
    this.isMarked.set(this.experiment.marked ?? false);
  }

  toggleMark(event: Event): void {
    event.stopPropagation();

    const wasMarked = this.isMarked();
    const nowMarked = !wasMarked;
    this.isMarked.set(nowMarked);

    const request$ = nowMarked
      ? this.experimentDetailService.mark(this.experiment.id)
      : this.experimentDetailService.unmark(this.experiment.id);

    request$.subscribe({
      error: () => {
        this.isMarked.set(wasMarked);
      },
    });
  }

  openDetails(): void {
    this.router.navigateByUrl(`/experiments/${this.experiment.id}`);
  }

  getStatusBadgeVariant = getExperimentStatusBadgeVariant;

  get statusLabel(): string {
    return this.experiment.status;
  }
}
