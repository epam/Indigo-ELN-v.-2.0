import { Component, computed, inject, Input, OnInit, signal, ViewChild, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../card/card.component';
import { CopyComponent } from '../copy/copy.component';
import { CounterComponent } from '../counter/counter.component';
import { DropdownMenuComponent } from '../dropdown-menu/dropdown-menu.component';
import { ProjectAcl, ProjectAclUpdate, UserSuggestion } from '@/core/types/entities/acl.i';
import { AclLevel, ELIGIBLE_ACL_LEVELS, isInmutableLevel } from '@/core/enums/acl-levels.enum';
import { ApiService } from '@/core/services/api.service';
import { finalize } from 'rxjs';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { ButtonComponent } from '../button/button.component';
import { NgSelectComponent, NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';
import { TeamComponentConfig } from './team.config';
import { InitialsPipe } from '../../../pipes/avatars.pipe';
import { TextOverflowTooltipDirective } from '@/core/directives/text-overflow-tooltip.directive';
import { MatDialog } from '@angular/material/dialog';
import {
  RemoveMemberConfirmationDialogComponent,
  RemoveMemberConfirmationResult,
} from '../remove-member-confirmation-dialog/remove-member-confirmation-dialog.component';

type UserSuggestionWithState = UserSuggestion & { added?: boolean };

interface TeamLoadingState {
  suggestions: boolean;
  addingUsers: boolean;
  updatingMembers: Set<string>;
}

@Component({
  selector: 'eln-team',
  templateUrl: './team.component.html',
  standalone: true,
  imports: [
    CommonModule,
    CounterComponent,
    CopyComponent,
    DropdownMenuComponent,
    CardComponent,
    NormalizeLabelPipe,
    ButtonComponent,
    NgSelectModule,
    FormsModule,
    InitialsPipe,
    TextOverflowTooltipDirective,
  ],
})
export class TeamComponent implements OnInit {
  @Input() entityId?: string;
  @Input() set team(value: ProjectAcl[]) {
    this._team.set(value);
    this.rebuildSuggestionsState();
  }
  private _team: WritableSignal<ProjectAcl[]> = signal<ProjectAcl[]>([]);
  @Input({ required: true }) config: TeamComponentConfig;

  userSuggestions: UserSuggestionWithState[] = [];
  selectedUserIds: string[] = [];

  loading = signal<TeamLoadingState>({
    suggestions: false,
    addingUsers: false,
    updatingMembers: new Set(),
  });

  teamVm = computed(() => {
    const updating = this.loading().updatingMembers;
    const base = this._team();
    return base.map((member) => ({
      ...member,
      disabled: isInmutableLevel(member.level) || updating.has(member.userId),
    }));
  });

  aclLevelOptions = ELIGIBLE_ACL_LEVELS;

  private api = inject(ApiService);
  private dialog = inject(MatDialog);

  @ViewChild(NgSelectComponent) ngSelectComponent!: NgSelectComponent;

  ngOnInit(): void {
    if (!this.entityId) console.warn('TeamComponent initialized without entityId');
    this.loading.update((l) => ({ ...l, suggestions: true }));
    this.api
      .request<UserSuggestion[]>('get', 'users/suggest')
      .pipe(finalize(() => this.loading.update((l) => ({ ...l, suggestions: false }))))
      .subscribe((list) => {
        this.userSuggestions = list;
        this.rebuildSuggestionsState();
      });
  }

  onUsersSelectedIds(ids: string[]): void {
    this.selectedUserIds = ids.filter((id) => !this.isUserInTeam(id));

    this.ngSelectComponent.searchTerm = '';
  }

  addSelectedUsers(): void {
    const endpoint = this.endpoint();
    if (!endpoint) {
      console.error('Cannot add users: missing entity id');
      return;
    }
    this.loading.update((l) => ({ ...l, addingUsers: true }));
    const existingPayload: ProjectAclUpdate[] = this._team().map((m) => ({
      userID: m.userId,
      level: m.level,
    }));
    const newPayload: ProjectAclUpdate[] = this.selectedUserIds.map((id) => ({
      userID: id,
      level: AclLevel.VIEW,
    }));
    const fullPayload: ProjectAclUpdate[] = [...existingPayload, ...newPayload];

    this.api
      .request<ProjectAclUpdate[] | ProjectAclUpdate>('post', endpoint, fullPayload)
      .pipe(
        finalize(() => {
          this.loading.update((l) => ({ ...l, addingUsers: false }));
        }),
      )
      .subscribe(() => this.handleSuccessfulUserAddition());
  }

  updateAclLevel(member: ProjectAcl, rawLevel: string): void {
    const newLevel = AclLevel[rawLevel as keyof typeof AclLevel];
    if (!newLevel) {
      console.error('Invalid ACL level:', rawLevel);
      return;
    }

    if (newLevel === AclLevel.NONE) {
      this.dialog
        .open<
          RemoveMemberConfirmationDialogComponent,
          { showCascadeCheckbox: boolean },
          RemoveMemberConfirmationResult
        >(RemoveMemberConfirmationDialogComponent, {
          data: { showCascadeCheckbox: true },
        })
        .afterClosed()
        .subscribe((result) => {
          if (!result?.confirmed) return;
          this.performAclUpdate(member, newLevel);
        });
      return;
    }

    this.performAclUpdate(member, newLevel);
  }

  private performAclUpdate(member: ProjectAcl, newLevel: AclLevel): void {
    const endpoint = this.endpoint();
    if (!endpoint) return;

    this.loading.update((l) => ({
      ...l,
      updatingMembers: new Set(l.updatingMembers).add(member.userId),
    }));
    this.api
      .request<ProjectAclUpdate>('post', endpoint, [{ userID: member.userId, level: newLevel }])
      .pipe(
        finalize(() => {
          this.loading.update((l) => {
            const after = new Set(l.updatingMembers);
            after.delete(member.userId);
            return { ...l, updatingMembers: after };
          });
        }),
      )
      .subscribe((projectAcl) => {
        if (projectAcl) {
          const updated = this._team().map((m) => (m.userId === member.userId ? { ...m, level: newLevel } : m));
          this._team.set(updated);
        }
      });
  }

  private endpoint(): string {
    const id = this.entityId;
    if (!id) return '';
    return this.config.buildAccessEndpoint(id);
  }

  private isUserInTeam(userId: string): boolean {
    return this._team().some((m) => m.userId === userId);
  }

  private rebuildSuggestionsState(): void {
    const teamIds = new Set(this._team().map((m) => m.userId));
    this.userSuggestions = this.userSuggestions.map((s) => ({
      ...s,
      added: teamIds.has(s.id),
    }));
  }

  private handleSuccessfulUserAddition(): void {
    const current = this._team();
    const toAdd: ProjectAcl[] = [];
    this.selectedUserIds.forEach((id) => {
      const suggestion = this.userSuggestions.find((u) => u.id === id);
      if (!suggestion) return;
      toAdd.push({
        userId: suggestion.id,
        username: suggestion.username,
        displayName: suggestion.displayName,
        level: AclLevel.VIEW,
        inherited: false,
      });
    });
    this._team.set([...current, ...toAdd]);
    this.rebuildSuggestionsState();
    this.selectedUserIds = [];
  }
}
