import { Component, inject, Input, OnDestroy } from '@angular/core';
// import { AvatarComponent } from '../../common/avatar/avatar.component';
import { CardComponent } from '../../common/card/card.component';
import { CopyComponent } from '../../common/copy/copy.component';
import { CounterComponent } from '../../common/counter/counter.component';
import { DropdownMenuComponent } from '../../common/dropdown-menu/dropdown-menu.component';
import { ProjectAcl } from '@/core/types/entities/acl.i';
import { AclLevel } from '@/core/enums/acl-levels.enum';
import { capitalize } from 'lodash';
import { Project } from '@/core/types/entities/project.i';
import { ApiService } from '@/core/services/api.service';
import { catchError, of, Subject, takeUntil } from 'rxjs';

const INMUTABLE_ACL_LEVELS = [ AclLevel.AUTHOR ]

@Component({
  selector: 'eln-team',
  templateUrl: './team.component.html',
  standalone: true,
  imports: [
    CounterComponent,
    // AvatarComponent,
    CopyComponent,
    DropdownMenuComponent,
    CardComponent,
  ],
})
export class TeamComponent implements OnDestroy {
  @Input() project: Project;
  @Input() team: ProjectAcl[] = [];
  private destroy$ = new Subject<void>();
  service = inject(ApiService);
  aclMembersLoading = new Set<string>(); // Tracks which members are loading

  normalizeLabel(key: string): string {
    return capitalize(key.toLowerCase().replaceAll("_", " "))
  }

  isInmutableLevel(level: AclLevel): boolean {
    return INMUTABLE_ACL_LEVELS.includes(level);
  }

  isAclMemberLoading(member: ProjectAcl): boolean {
    return this.aclMembersLoading.has(member.userId);
  }

  aclLevelOptions = Object.values(AclLevel)
    .filter((value) => !this.isInmutableLevel(value))
    .map((value) => ({
      label: this.normalizeLabel(value),
      value
    }));

  updateAclLevel(member: ProjectAcl, rawLevel: string): void {
    const newLevel = AclLevel[rawLevel as keyof typeof AclLevel];

    if (!newLevel) {
      console.error('Invalid ACL level:', rawLevel);
      return;
    }

    // Add member to loading set
    this.aclMembersLoading.add(member.userId);

    this.service.request<Pick<ProjectAcl, 'userId' | 'level'>>('post', `projects/${this.project.id}/access`, [{ userID: member.userId, level: newLevel }])
      .pipe(
        takeUntil(this.destroy$),
        catchError((err) => {
          console.error('Failed to update ACL level:', err);
          // Remove from loading set on error
          this.aclMembersLoading.delete(member.userId);
          return of(null);
        })
      )
      .subscribe({
        next: (projectAcl) => {
          if (projectAcl) member.level = newLevel;
          // Remove from loading set on success
          this.aclMembersLoading.delete(member.userId);
        },
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.aclMembersLoading.clear();
  }
}