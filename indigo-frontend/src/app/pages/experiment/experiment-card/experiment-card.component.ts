import { AvatarComponent } from '@/core/components/common/avatar/avatar.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { Experiment } from '@/core/types/entities/experiment.i';
import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'eln-experiment-card',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    AvatarComponent
  ],
  templateUrl: './experiment-card.component.html',
  styleUrls: ['./experiment-card.component.scss']
})
export class ExperimentCardComponent {
  mock_users = [
    'assets/avatar1.png',
    'assets/avatar2.png',
    'assets/avatar3.png',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
  ];
  @Input() experiment: Experiment;
  @Input() variant: 'grid' | 'list' = 'grid';
  isFavorite: boolean = false;
  toggleFavorite(event: MouseEvent): void {
    event.stopPropagation(); // Prevent card click
    this.isFavorite = !this.isFavorite;
    // Optionally persist the favorite state to backend or service
  }

  getStatusClass(status: string): string {
    switch (status?.toLowerCase()) {
      case 'open':
        return 'status-open';
      case 'waiting for signature':
        return 'status-waiting-for-signature';
      case 'completed':
        return 'status-completed';
      case 'rejected':
        return 'status-rejected';
      default:
        return 'status-open'; // fallback
    }
  }

}
