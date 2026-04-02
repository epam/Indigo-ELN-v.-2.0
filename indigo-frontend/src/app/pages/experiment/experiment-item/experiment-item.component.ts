import { BadgeComponent } from '@/core/components/common/badge/badge.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ExperimentDetail } from '@/core/types/entities/experiments/experiment-detail.i';
import { CommonModule } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltip } from '@angular/material/tooltip';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { InitialsPipe } from '@/core/pipes/avatars.pipe';
import { getExperimentStatusBadgeVariant } from '@/core/utils/experiment-status.util';

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
  @Input() projectId!: string;
  @Input() notebookId!: string;

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