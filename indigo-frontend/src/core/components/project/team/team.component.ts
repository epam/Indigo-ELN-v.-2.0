import { Component, inject, Input } from '@angular/core';
// import { AvatarComponent } from '../../common/avatar/avatar.component';
import { ButtonComponent } from '../../common/button/button.component';
import { CardComponent } from '../../common/card/card.component';
import { CopyComponent } from '../../common/copy/copy.component';
import { CounterComponent } from '../../common/counter/counter.component';
import { DropdownMenuComponent } from '../../common/dropdown-menu/dropdown-menu.component';
import { ProjectAcl } from '@/core/types/entities/acl.i';
import { AclLevels } from '@/core/enums/acl-levels.enum';
import { capitalize } from 'lodash';
import { Project } from '@/core/types/entities/project.i';
import { ApiService } from '@/core/services/api.service';
import { catchError, of, Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'eln-team',
  templateUrl: './team.component.html',
  standalone: true,
  imports: [
    CounterComponent,
    ButtonComponent,
    // AvatarComponent,
    CopyComponent,
    DropdownMenuComponent,
    CardComponent,
  ],
})
export class TeamComponent {
  @Input() project: Project;
  @Input() team: ProjectAcl[] = [];
  private destroy$ = new Subject<void>();
  service = inject(ApiService);

  normalizeLabel(key: string): string {
    return capitalize(key.toLowerCase().replaceAll("_", " "))
  }

  aclLevelOptions = Object.entries(AclLevels).map(([key, value]) => ({
    label: this.normalizeLabel(key),
    value
  }));

  updateAclLevel(member: ProjectAcl, newLevel: string) {
    console.log('Updating ACL level for member:', member, 'to new level:', newLevel);
    this.service.request<Pick<ProjectAcl, 'userId' | 'level'>>('post', `projects/${this.project.id}/access`, [{ userID: member.userId, level: newLevel }])
      .pipe(
        takeUntil(this.destroy$),
        catchError((err) => {
          // TODO display error message to user (maybe a toast or popup notification?)
          console.error('Failed to update ACL level:', err);
          return of(null);
        })
      )
      .subscribe({
        next: (projectAcl) => {
          if (projectAcl) member.level = newLevel;
        },
      });
  }
}