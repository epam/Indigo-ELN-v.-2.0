import { AvatarComponent } from '@/core/components/common/avatar/avatar.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { Notebook } from '@/core/types/entities/notebook.i';
import { CommonModule } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'eln-notebook-item',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    AvatarComponent,
  ],
  templateUrl: './notebook-item.component.html',
})
export class NotebookItemComponent {
  private router = inject(Router);
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
  @Input() notebook: Notebook;
  @Input() variant: 'grid' | 'list' = 'grid';
  @Input() projectId: string;

  openDetails(): void {
    if (!this.notebook || !this.projectId) return;
    const url = `/projects/${this.projectId}/notebooks/${this.notebook.id}`;
    this.router.navigateByUrl(url);
  }
}
