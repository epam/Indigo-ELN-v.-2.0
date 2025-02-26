import { Component, Input } from '@angular/core';
import { AvatarComponent } from '../../common/avatar/avatar.component';
import { ButtonComponent } from '../../common/button/button.component';
import { CardComponent } from '../../common/card/card.component';
import { CopyComponent } from '../../common/copy/copy.component';
import { CounterComponent } from '../../common/counter/counter.component';
import { DropdownMenuComponent } from '../../common/dropdown-menu/dropdown-menu.component';
@Component({
  selector: 'app-team',
  templateUrl: './team.component.html',
  standalone: true,
  imports: [
    CounterComponent,
    ButtonComponent,
    AvatarComponent,
    CopyComponent,
    DropdownMenuComponent,
    CardComponent,
  ],
})
export class TeamComponent {
  // TODO: fix type
  @Input() team: any;
}
