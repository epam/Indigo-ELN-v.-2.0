import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltip } from '@angular/material/tooltip';
import { Router } from '@angular/router';

import { CardComponent } from '@/core/components/common/card/card.component';
import { Notebook } from '@/core/types/entities/notebook.i';
import { InitialsPipe } from '../../../../core/pipes/avatars.pipe';

@Component({
  selector: 'eln-notebook-item',
  standalone: true,
  imports: [CommonModule, CardComponent, MatButtonModule, MatIconModule, MatMenuModule, InitialsPipe, MatTooltip],
  templateUrl: './notebook-item.component.html',
})
export class NotebookItemComponent {
  private router = inject(Router);

  @Input() notebook: Notebook;
  @Input() variant: 'grid' | 'list' = 'grid';

  openDetails(): void {
    if (!this.notebook) {
      return;
    }

    this.router.navigateByUrl(`/notebooks/${this.notebook.id}`);
  }
}
