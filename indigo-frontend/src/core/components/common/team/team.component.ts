import { TextOverflowTooltipDirective } from '@/core/directives/text-overflow-tooltip.directive';
import { AclLevel, ELIGIBLE_ACL_LEVELS, isInmutableLevel } from '@/core/enums/acl-levels.enum';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { ApiService } from '@/core/services/api.service';
import { ACLEntry, ACLUpdate } from '@/core/types/entities/acl.i';
import { UserRef } from '@/core/types/entities/user.i';
import { CommonModule } from '@angular/common';
import { Component, computed, inject, Input, OnInit, signal, ViewChild, WritableSignal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { SvgIconComponent } from '@core/components/common/svg-icon/svg-icon.component';
import { NgSelectComponent, NgSelectModule } from '@ng-select/ng-select';
import { finalize } from 'rxjs';
import { InitialsPipe } from '../../../pipes/avatars.pipe';
import { ButtonComponent } from '../button/button.component';
import { CardComponent } from '../card/card.component';
import { CopyComponent } from '../copy/copy.component';
import { CounterComponent } from '../counter/counter.component';
import { DropdownMenuComponent } from '../dropdown-menu/dropdown-menu.component';
import {
  RemoveMemberConfirmationDialogComponent,
  RemoveMemberConfirmationResult,
} from '../remove-member-confirmation-dialog/remove-member-confirmation-dialog.component';
import { TeamComponentConfig } from './team.config';

type UserRefWithState = UserRef & { added?: boolean };

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
    SvgIconComponent,
  ],
})
export class TeamComponent implements OnInit {
  @Input() entityId?: string;
  @Input() set team(value: ACLEntry[]) {
    this._team.set(value);
    this.rebuildSuggestionsState();
  }
  private _team: WritableSignal<ACLEntry[]> = signal<ACLEntry[]>([]);
  @Input({ required: true }) config: TeamComponentConfig;

  userSuggestions: UserRefWithState[] = [];
  selectedUsers: string[] = [];

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
      disabled: isInmutableLevel(member.level) || updating.has(member.username),
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
      .request<UserRef[]>('get', 'users/suggest')
      .pipe(finalize(() => this.loading.update((l) => ({ ...l, suggestions: false }))))
      .subscribe((list) => {
        this.userSuggestions = list;
        this.rebuildSuggestionsState();
      });
  }

  onUsersSelected(usernames: string[]): void {
    this.selectedUsers = usernames.filter((username) => !this.isUserInTeam(username));

    this.ngSelectComponent.searchTerm = '';
  }

  addSelectedUsers(): void {
    const endpoint = this.endpoint();
    if (!endpoint) {
      console.error('Cannot add users: missing entity id');
      return;
    }
    this.loading.update((l) => ({ ...l, addingUsers: true }));
    const existingPayload: ACLUpdate[] = this._team().map((m) => ({
      username: m.username,
      level: m.level,
    }));
    const newPayload: ACLUpdate[] = this.selectedUsers.map((username) => ({
      username: username,
      level: AclLevel.VIEW,
    }));
    const fullPayload: ACLUpdate[] = [...existingPayload, ...newPayload];

    this.api
      .request<ACLUpdate[] | ACLUpdate>('post', endpoint, fullPayload)
      .pipe(
        finalize(() => {
          this.loading.update((l) => ({ ...l, addingUsers: false }));
        }),
      )
      .subscribe(() => this.handleSuccessfulUserAddition());
  }

  updateAclLevel(member: ACLEntry, rawLevel: string): void {
    const newLevel = AclLevel[rawLevel as keyof typeof AclLevel];
    if (!newLevel) {
      console.error('Invalid ACL level:', rawLevel);
      return;
    }

    if (newLevel === AclLevel.NONE) {
      this.dialog
        .open(RemoveMemberConfirmationDialogComponent, {
          data: {
            showCascadeCheckbox: true,
            title: 'Remove Member',
            message: `Are you sure you want to remove ${member.displayName || member.username} from the team?`,
            confirmButtonLabel: 'Remove',
            cancelButtonLabel: 'Cancel',
          },
        })
        .afterClosed()
        .subscribe((result?: RemoveMemberConfirmationResult) => {
          if (!result?.confirmed) return;
          this.applyAclLevelUpdate(member, newLevel);
        });
      return;
    }

    this.applyAclLevelUpdate(member, newLevel);
  }

  private applyAclLevelUpdate(member: ACLEntry, newLevel: AclLevel): void {
    const endpoint = this.endpoint();
    if (!endpoint) return;

    this.loading.update((l) => ({
      ...l,
      updatingMembers: new Set(l.updatingMembers).add(member.username),
    }));
    this.api
      .request<ACLUpdate>('post', endpoint, [{ username: member.username, level: newLevel }])
      .pipe(
        finalize(() => {
          this.loading.update((l) => {
            const after = new Set(l.updatingMembers);
            after.delete(member.username);
            return { ...l, updatingMembers: after };
          });
        }),
      )
      .subscribe((projectAcl) => {
        if (projectAcl) {
          const updated = this._team().map((m) => (m.username === member.username ? { ...m, level: newLevel } : m));
          this._team.set(updated);
        }
      });
  }

  private endpoint(): string {
    const id = this.entityId;
    if (!id) return '';
    return this.config.buildAccessEndpoint(id);
  }

  private isUserInTeam(username: string): boolean {
    return this._team().some((m) => m.username === username);
  }

  private rebuildSuggestionsState(): void {
    const teamUsernames = new Set(this._team().map((m) => m.username));
    this.userSuggestions = this.userSuggestions.map((s) => ({
      ...s,
      added: teamUsernames.has(s.username),
    }));
  }

  private handleSuccessfulUserAddition(): void {
    const current = this._team();
    const toAdd: ACLEntry[] = [];
    this.selectedUsers.forEach((username) => {
      const suggestion = this.userSuggestions.find((u) => u.username === username);
      if (!suggestion) return;
      toAdd.push({
        username: suggestion.username,
        displayName: suggestion.displayName,
        level: AclLevel.VIEW,
        inherited: false,
      });
    });
    this._team.set([...current, ...toAdd]);
    this.rebuildSuggestionsState();
    this.selectedUsers = [];
  }
}
