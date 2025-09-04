import { Component, Input, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../card/card.component';
import { CopyComponent } from '../copy/copy.component';
import { CounterComponent } from '../counter/counter.component';
import { DropdownMenuComponent } from '../dropdown-menu/dropdown-menu.component';
import { ProjectAcl, ProjectAclUpdate, UserSuggestion } from '@/core/types/entities/acl.i';
import { AclLevel, ELIGIBLE_ACL_LEVELS, isInmutableLevel } from '@/core/enums/acl-levels.enum';
import { ApiService } from '@/core/services/api.service';
import { catchError, of, Subject, takeUntil } from 'rxjs';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { ButtonComponent } from '../button/button.component';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';

// Config interface to adapt component to entity context (project, notebook, etc.)
export interface TeamComponentConfig {
    title?: string; // e.g. 'Team' or 'Notebook Team'
    buildAccessEndpoint: (entityId: string) => string; // e.g. projects/{id}/access or notebooks/{id}/access
}

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
export class TeamComponent implements OnInit, OnDestroy {
    @Input() entityId?: string;
    @Input({ required: true }) team: ProjectAcl[] = [];
    @Input({ required: true }) config: TeamComponentConfig;

    userSuggestions: UserSuggestionWithState[] = [];
    selectedUserIds: string[] = [];
    addUserLoading = false;
    aclMembersLevelLoading = new Set<string>();

    aclLevelOptions = ELIGIBLE_ACL_LEVELS;
    isInmutableLevel = isInmutableLevel;

    private destroy$ = new Subject<void>();
    private api = inject(ApiService);

    get addMemberLabelMap(): Record<string, string> {
        return {
            '=0': 'Add member',
            '=1': 'Add member',
            other: 'Add # members'
        };
    }

    get title(): string { return this.config.title || 'Team'; }

    private resolvedEntityId(): string | undefined {
        return this.entityId;
    }

    private endpoint(): string {
        const id = this.resolvedEntityId();
        if (!id) return '';
        return this.config.buildAccessEndpoint(id);
    }

    onUsersSelectedIds(ids: string[]): void {
        this.selectedUserIds = ids.filter(id => !this.isUserInTeam(id));
    }

    private isUserInTeam(userId: string): boolean {
        return this.team.some(m => m.userId === userId);
    }

    private rebuildSuggestionsState(): void {
        const teamIds = new Set(this.team.map(m => m.userId));
        this.userSuggestions = this.userSuggestions.map(s => ({ ...s, added: teamIds.has(s.id) }));
    }

    isAclMemberLoading(member: ProjectAcl): boolean { return this.aclMembersLevelLoading.has(member.userId); }

    addSelectedUsers(): void {
        const endpoint = this.endpoint();
        if (!endpoint) {
            console.error('Cannot add users: missing entity id');
            return;
        }
        this.addUserLoading = true;
        const existingPayload: ProjectAclUpdate[] = this.team.map(m => ({ userID: m.userId, level: m.level }));
        const newPayload: ProjectAclUpdate[] = this.selectedUserIds.map(id => ({ userID: id, level: AclLevel.VIEW }));
        const fullPayload: ProjectAclUpdate[] = [...existingPayload, ...newPayload];

        this.api.request<ProjectAclUpdate[] | ProjectAclUpdate | null>('post', endpoint, fullPayload)
            .pipe(
                takeUntil(this.destroy$),
                catchError(err => {
                    console.error('Failed to update ACL with new users:', err);
                    this.addUserLoading = false;
                    return of(null);
                })
            )
            .subscribe(resp => {
                if (resp !== null) {
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
                }
                this.addUserLoading = false;
            });
    }

    updateAclLevel(member: ProjectAcl, rawLevel: string): void {
        const newLevel = AclLevel[rawLevel as keyof typeof AclLevel];
        if (!newLevel) { console.error('Invalid ACL level:', rawLevel); return; }
        const endpoint = this.endpoint();
        if (!endpoint) return;

        this.aclMembersLevelLoading.add(member.userId);
        this.api.request<ProjectAclUpdate>('post', endpoint, [{ userID: member.userId, level: newLevel }])
            .pipe(catchError(err => { console.error('Failed to update ACL level:', err); this.aclMembersLevelLoading.delete(member.userId); return of(null); }))
            .subscribe(projectAcl => {
                if (projectAcl) member.level = newLevel;
                this.aclMembersLevelLoading.delete(member.userId);
            });
    }

    ngOnInit(): void {
        if (!this.resolvedEntityId()) {
            console.warn('TeamComponent initialized without entityId or project.id');
        }
        this.api.request<UserSuggestion[]>('get', 'users/suggest')
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: list => { this.userSuggestions = list; this.rebuildSuggestionsState(); },
                error: err => console.error('Failed to load suggestions', err)
            });
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
        this.aclMembersLevelLoading.clear();
    }
}
