import { Component, inject, Input, OnDestroy, OnInit } from '@angular/core';
// import { AvatarComponent } from '../../common/avatar/avatar.component';
import { CardComponent } from '../../common/card/card.component';
import { CopyComponent } from '../../common/copy/copy.component';
import { CounterComponent } from '../../common/counter/counter.component';
import { DropdownMenuComponent } from '../../common/dropdown-menu/dropdown-menu.component';
import { ProjectAcl, ProjectAclUpdate, UserSuggestion } from '@/core/types/entities/acl.i';
import { AclLevel, ELIGIBLE_ACL_LEVELS, isInmutableLevel } from '@/core/enums/acl-levels.enum';
import { Project } from '@/core/types/entities/project.i';
import { ApiService } from '@/core/services/api.service';
import { catchError, of, Subject, takeUntil } from 'rxjs';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { ButtonComponent } from "../../common/button/button.component";
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';

type UserSuggestionWithState = UserSuggestion & { added?: boolean };

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
    NormalizeLabelPipe,
    ButtonComponent,
    NgSelectModule,
    FormsModule
  ],
})
export class TeamComponent implements OnDestroy, OnInit {
  aclLevelOptions = ELIGIBLE_ACL_LEVELS;
  isInmutableLevel = isInmutableLevel;

  @Input({ required: true }) project: Project;
  @Input({ required: true }) team: ProjectAcl[] = [];

  // Single suggestions list enriched with added state
  userSuggestions: UserSuggestionWithState[] = [];

  private destroy$ = new Subject<void>();
  service = inject(ApiService);
  addUserLoading = false;
  aclMembersLevelLoading = new Set<string>(); // Tracks which members are loading
  selectedUserIds: string[] = [];

  // Check if user is already in the team
  private isUserInTeam(userId: string): boolean {
    return this.team.some(m => m.userId === userId);
  }

  // Rebuild added flag on suggestions based on current team
  // Those users already in the team should be marked as added
  private rebuildSuggestionsState(): void {
    this.userSuggestions.forEach(s => { s.added = this.isUserInTeam(s.id); });
  }

  isAclMemberLoading(member: ProjectAcl): boolean {
    return this.aclMembersLevelLoading.has(member.userId);
  }

  onUsersSelectedIds(ids: string[]): void {
    this.selectedUserIds = ids.filter(id => !this.isUserInTeam(id));
  }

  addSelectedUsers(): void {
    this.addUserLoading = true;

    const existingPayload: ProjectAclUpdate[] = this.team.map(m => ({ userID: m.userId, level: m.level }));
    const newPayload: ProjectAclUpdate[] = this.selectedUserIds.map(id => ({ userID: id, level: AclLevel.VIEW }));
    const fullPayload: ProjectAclUpdate[] = [...existingPayload, ...newPayload];

    this.service.request<ProjectAclUpdate[] | ProjectAclUpdate | null>(
      'post',
      `projects/${this.project.id}/access`,
      fullPayload
    )
      .pipe(
        takeUntil(this.destroy$),
        catchError((err) => {
          console.error('Failed to update project ACL with new users:', err);
          this.addUserLoading = false;
          return of(null);
        })
      )
      .subscribe({
        next: (resp) => {
          if (resp === null) return;

          this.selectedUserIds.forEach(id => {
            const suggestion = this.userSuggestions.find(u => u.id === id);
            if (!suggestion) return;
            this.team.push({
              userId: suggestion.id,
              username: suggestion.username,
              displayName: suggestion.displayName,
              level: AclLevel.VIEW, // Assumed default level for new users
              inherited: false // Assumed default inherited state for new users
            });
          });

          this.rebuildSuggestionsState();
          this.selectedUserIds = [];
        },
        error: (err) => {
          console.error('Failed to update project ACL with new users:', err);
        },
        complete: () => {
          this.addUserLoading = false;
        }
      });
  }
  updateAclLevel(member: ProjectAcl, rawLevel: string): void {
    const newLevel = AclLevel[rawLevel as keyof typeof AclLevel];

    if (!newLevel) {
      console.error('Invalid ACL level:', rawLevel);
      return;
    }

    // Add member to loading set
    this.aclMembersLevelLoading.add(member.userId);

    this.service.request<ProjectAclUpdate>('post', `projects/${this.project.id}/access`, [{ userID: member.userId, level: newLevel }])
      .pipe(
        takeUntil(this.destroy$),
        catchError((err) => {
          console.error('Failed to update ACL level:', err);
          // Remove from loading set on error
          this.aclMembersLevelLoading.delete(member.userId);
          return of(null);
        })
      )
      .subscribe({
        next: (projectAcl) => {
          if (projectAcl) member.level = newLevel;
          // Remove from loading set on success
          this.aclMembersLevelLoading.delete(member.userId);
        },
      });
  }

  ngOnInit(): void {
    this.service.request<UserSuggestion[]>('get', `users/suggest`)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (userList) => {
          this.userSuggestions = userList;
          this.rebuildSuggestionsState();
        },
        error: (err) => {
          console.error('Failed to load team members:', err);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.aclMembersLevelLoading.clear();
  }
}