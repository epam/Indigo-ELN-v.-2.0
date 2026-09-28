import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { Router } from '@angular/router';
import { MemberAvatarsComponent } from '@core/components/common/member-avatars/member-avatars.component';

import { CardComponent } from '@/core/components/common/card/card.component';
import { Notebook } from '@/core/types/entities/notebook.i';

@Component({
  selector: 'eln-notebook-item',
  standalone: true,
  imports: [CommonModule, CardComponent, MatButtonModule, MatIconModule, MatMenuModule, MemberAvatarsComponent],
  templateUrl: './notebook-item.component.html',
})
export class NotebookItemComponent {
  private router = inject(Router);

  @Input({ required: true }) notebook: Notebook;
  @Input() variant: 'grid' | 'list' = 'grid';

  openDetails(): void {
    this.router.navigateByUrl(`/notebooks/${this.notebook.id}`);
  }
}
