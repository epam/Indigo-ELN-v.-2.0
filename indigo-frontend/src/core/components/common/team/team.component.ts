import { Component, Input, OnInit, inject, signal, computed, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../card/card.component';
import { CopyComponent } from '../copy/copy.component';
import { CounterComponent } from '../counter/counter.component';
import { DropdownMenuComponent } from '../dropdown-menu/dropdown-menu.component';
import { ProjectAcl, ProjectAclUpdate, UserSuggestion } from '@/core/types/entities/acl.i';
import { AclLevel, ELIGIBLE_ACL_LEVELS, isInmutableLevel } from '@/core/enums/acl-levels.enum';
import { ApiService } from '@/core/services/api.service';
import { catchError, finalize, of } from 'rxjs';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { ButtonComponent } from '../button/button.component';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';
import { TeamComponentConfig } from './team.config';

type UserSuggestionWithState = UserSuggestion & { added?: boolean };

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
        FormsModule
    ],
})
export class TeamComponent implements OnInit {
    @Input() entityId?: string;
    @Input() set teamInput(value: ProjectAcl[]) {
        this.team.set(value);
        this.rebuildSuggestionsState();
    }
    // Internal mutable state (optimistic / UI state)
    private team: WritableSignal<ProjectAcl[]> = signal<ProjectAcl[]>([]);
    @Input({ required: true }) config: TeamComponentConfig;

    userSuggestions: UserSuggestionWithState[] = [];
    selectedUserIds: string[] = [];
    addUserLoading = false;

    // Set of members currently updating ACL level
    private _loadingMembers = signal<Set<string>>(new Set());

    // View model with derived UI-only flags (disabled)
    teamVm = computed(() => {
        const loading = this._loadingMembers();
        const base = this.team();
        return base.map(member => ({
            ...member,
            disabled: isInmutableLevel(member.level) || loading.has(member.userId)
        }));
    });

    aclLevelOptions = ELIGIBLE_ACL_LEVELS;

    private api = inject(ApiService);

    get addMemberLabelMap(): Record<string, string> {
        return {
            '=0': 'Add member',
            '=1': 'Add member',
            other: 'Add # members'
        };
    }

    private endpoint(): string {
        const id = this.entityId;
        if (!id) return '';
        return this.config.buildAccessEndpoint(id);
    }

    onUsersSelectedIds(ids: string[]): void {
        this.selectedUserIds = ids.filter(id => !this.isUserInTeam(id));
    }

    private isUserInTeam(userId: string): boolean { return this.team().some(m => m.userId === userId); }

    private rebuildSuggestionsState(): void {
        const teamIds = new Set(this.team().map(m => m.userId));
        this.userSuggestions = this.userSuggestions.map(s => ({ ...s, added: teamIds.has(s.id) }));
    }

    addSelectedUsers(): void {
        const endpoint = this.endpoint();
        if (!endpoint) {
            console.error('Cannot add users: missing entity id');
            return;
        }
        this.addUserLoading = true;
        const existingPayload: ProjectAclUpdate[] = this.team().map(m => ({ userID: m.userId, level: m.level }));
        const newPayload: ProjectAclUpdate[] = this.selectedUserIds.map(id => ({ userID: id, level: AclLevel.VIEW }));
        const fullPayload: ProjectAclUpdate[] = [...existingPayload, ...newPayload];

        this.api.request<ProjectAclUpdate[] | ProjectAclUpdate | null>('post', endpoint, fullPayload)
            .pipe(
                catchError(err => {
                    console.error('Failed to update ACL with new users:', err);
                    this.addUserLoading = false;
                    return of(null);
                })
            )
            .subscribe(resp => {
                if (resp !== null) {
                    const current = this.team();
                    const toAdd: ProjectAcl[] = [];
                    this.selectedUserIds.forEach(id => {
                        const suggestion = this.userSuggestions.find(u => u.id === id);
                        if (!suggestion) return;
                        toAdd.push({
                            userId: suggestion.id,
                            username: suggestion.username,
                            displayName: suggestion.displayName,
                            level: AclLevel.VIEW, // Assumed default level for new users
                            inherited: false // Assumed default inherited state for new users
                        });
                    });
                    this.team.set([...current, ...toAdd]);
                    this.rebuildSuggestionsState();
                    this.selectedUserIds = [];
                }
                this.addUserLoading = false;
            });
    }

    updateAclLevel(member: ProjectAcl, rawLevel: string): void {
        const newLevel = AclLevel[rawLevel as keyof typeof AclLevel];
        if (!newLevel) { console.error('Invalid ACL level:', rawLevel); return; }
        const endpoint = this.endpoint();
        if (!endpoint) return;

        this._loadingMembers.update(members => new Set(members).add(member.userId));
        this.api.request<ProjectAclUpdate>('post', endpoint, [{ userID: member.userId, level: newLevel }])
            .pipe(
                catchError(err => {
                    console.error('Failed to update ACL level:', err);
                    return of(null);
                }),
                finalize(() => {
                    const after = new Set(this._loadingMembers());
                    after.delete(member.userId);
                    this._loadingMembers.set(after);
                })
            )
            .subscribe(projectAcl => {
                if (projectAcl) {
                    const updated = this.team().map(m => m.userId === member.userId ? { ...m, level: newLevel } : m);
                    this.team.set(updated);
                }
            });
    }

    ngOnInit(): void {
        if (!this.entityId) console.warn('TeamComponent initialized without entityId');
        // Effect handles initial sync and subsequent changes from parent
        this.api.request<UserSuggestion[]>('get', 'users/suggest')
            .subscribe({
                next: list => { this.userSuggestions = list; this.rebuildSuggestionsState(); },
                error: err => console.error('Failed to load suggestions', err)
            });
    }
}
