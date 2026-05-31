import { BadgeComponent } from '@/core/components/common/badge/badge.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { InitialsPipe } from '@/core/pipes/avatars.pipe';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { ExperimentDetail } from '@/core/types/entities/experiments/experiment-detail.i';
import { getExperimentStatusBadgeVariant } from '@/core/utils/experiment-status.util';
import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltip } from '@angular/material/tooltip';
import { Router } from '@angular/router';

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
  ],
  templateUrl: './experiment-item.component.html',
})
export class ExperimentItemComponent {
  private router = inject(Router);

  @Input() experiment!: ExperimentDetail;
  @Input() variant: 'grid' | 'list' = 'grid';

  openDetails(): void {
    if (!this.experiment) {
      return;
    }

    this.router.navigateByUrl(`/experiments/${this.experiment.id}`);
  }

  getStatusBadgeVariant = getExperimentStatusBadgeVariant;

  get statusLabel(): string {
    return this.experiment.status;
  }
}
