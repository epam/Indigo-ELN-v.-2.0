import { Project } from '@/core/types/entities/project.i';
import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { RouterLink } from '@angular/router';
import { CardComponent } from '@core/components/common/card/card.component';
import { MemberAvatarsComponent } from '@core/components/common/member-avatars/member-avatars.component';

@Component({
  selector: 'eln-project-item',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    RouterLink,
    MemberAvatarsComponent,
  ],
  templateUrl: './project-item.component.html',
  styleUrls: ['./project-item.component.scss'],
})
export class ProjectItemComponent {
  @Input() project: Project;
  @Input() variant: 'grid' | 'list' = 'grid';
}
