import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTooltipModule } from '@angular/material/tooltip';
import { InitialsPipe } from '@core/pipes/avatars.pipe';
import { ACLEntry } from '@core/types/entities/acl.i';

@Component({
  selector: 'eln-member-avatars',
  standalone: true,
  imports: [CommonModule, MatTooltipModule, InitialsPipe],
  templateUrl: './member-avatars.component.html',
  styleUrls: ['./member-avatars.component.scss'],
})
export class MemberAvatarsComponent {
  members = input<ACLEntry[]>([]);
  totalCount = input<number>(0);
  maxVisible = input<number>(3);

  visibleMembers = computed(() => {
    return this.members().slice(0, this.maxVisible());
  });
}
