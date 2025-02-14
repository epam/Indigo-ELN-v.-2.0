import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AvatarComponent } from '../../common/avatar/avatar.component';
import { CardComponent } from '../../common/card/card.component';

@Component({
  selector: 'app-project-item',
  templateUrl: './project-item.component.html',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatMenuModule,
    MatIconModule,
    AvatarComponent,
  ],
})
export class ProjectItemComponent {
  //TODO: Proper typing
  @Input() project: any;
}
