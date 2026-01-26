import { CardComponent } from '@/core/components/common/card/card.component';
import { Notebook } from '@/core/types/entities/notebook.i';
import { CommonModule } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { InitialsPipe } from '../../../../core/pipes/avatars.pipe';

@Component({
  selector: 'eln-notebook-item',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    InitialsPipe,
  ],
  templateUrl: './notebook-item.component.html',
})
export class NotebookItemComponent {
  private router = inject(Router);
  @Input() notebook: Notebook;
  @Input() variant: 'grid' | 'list' = 'grid';
  @Input() projectId: string;

  openDetails(): void {
    if (!this.notebook || !this.projectId) return;
    const url = `/projects/${this.projectId}/notebooks/${this.notebook.id}`;
    this.router.navigateByUrl(url);
  }
}
