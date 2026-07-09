import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog } from '@angular/material/dialog';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { ExperimentStatus } from '@core/enums/experiment-status.enum';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';
import { SignDialogComponent } from './sign-dialog/sign-dialog.component';
import { take } from 'rxjs';
import { DownloadService } from '@core/services/download.service';
import { BadgeComponent } from '@core/components/common/badge/badge.component';
import { NormalizeLabelPipe } from '@core/pipes/normalizeLabe.pipe';
import { EXPERIMENT_STATUS_DECORATION_MAP } from '@core/utils/experiment-status.util';
import { SlideInPanelService } from '@core/components/common/slide-in-panel/slide-in-panel.service';
import { ExperimentTeamDrawerComponent } from '@pages/experiment/experiment-team-drawer/experiment-team-drawer.component';
import { SvgIconComponent } from '@core/components/common/svg-icon/svg-icon.component';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { MemberAvatarsComponent } from '@core/components/common/member-avatars/member-avatars.component';
import { ButtonVariants } from '@core/components/common/button/button.variant';

enum Action {
  COMPLETE = 'COMPLETE',
  COMPLETE_AND_SIGN = 'COMPLETE_AND_SIGN',
  REOPEN = 'REOPEN',
  CANCEL = 'CANCEL',
  SUBMIT = 'SUBMIT',
  RESUBMIT = 'RESUBMIT',
  PRINT = 'PRINT',
}

interface ActionButton {
  action: Action;
  title: string;
  variant: ButtonVariants['variant'];
  iconClasses: string;
  allowedStatuses?: ExperimentStatus[];
  inDetailsMenu: boolean;
}

const BUTTONS: ActionButton[] = [
  {
    action: Action.COMPLETE,
    title: 'Complete',
    variant: 'blue',
    iconClasses: 'indicon-check-circle',
    allowedStatuses: [ExperimentStatus.OPEN, ExperimentStatus.REOPEN],
    inDetailsMenu: false,
  },
  {
    action: Action.COMPLETE_AND_SIGN,
    title: 'Complete and Sign',
    variant: 'blue',
    iconClasses: 'indicon-check-circle',
    allowedStatuses: [ExperimentStatus.OPEN, ExperimentStatus.REOPEN],
    inDetailsMenu: false,
  },
  {
    action: Action.REOPEN,
    title: 'Reopen',
    variant: 'blue',
    iconClasses: 'indicon-edit',
    allowedStatuses: [
      ExperimentStatus.COMPLETED,
      ExperimentStatus.SUBMITTED,
      ExperimentStatus.SIGNING,
      ExperimentStatus.REJECTED,
      ExperimentStatus.SIGNED,
      ExperimentStatus.ARCHIVED,
      ExperimentStatus.CANCELLED,
    ],
    inDetailsMenu: false,
  },
  {
    action: Action.CANCEL,
    title: 'Cancel',
    variant: 'red-outline',
    iconClasses: 'indicon-close',
    allowedStatuses: [ExperimentStatus.OPEN, ExperimentStatus.REOPEN],
    inDetailsMenu: false,
  },
  {
    action: Action.SUBMIT,
    title: 'Submit',
    variant: 'blue',
    iconClasses: 'indicon-paperclip',
    allowedStatuses: [ExperimentStatus.COMPLETED],
    inDetailsMenu: false,
  },
  {
    action: Action.RESUBMIT,
    title: 'Resubmit',
    variant: 'blue',
    iconClasses: 'indicon-paperclip',
    allowedStatuses: [ExperimentStatus.REJECTED],
    inDetailsMenu: false,
  },
  {
    action: Action.PRINT,
    title: 'Print',
    variant: 'grey',
    iconClasses: 'indicon-printer',
    inDetailsMenu: true,
  },
];

@Component({
  selector: 'eln-experiment-actions',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatMenuModule,
    BadgeComponent,
    NormalizeLabelPipe,
    SvgIconComponent,
    ButtonComponent,
    MemberAvatarsComponent,
  ],
  templateUrl: './experiment-actions.component.html',
})
export class ExperimentActionsComponent {
  experimentDetailService = inject(ExperimentDetailService);
  downloadService = inject(DownloadService);
  notificationService = inject(NotificationService);
  dialog = inject(MatDialog);
  slideInPanelService = inject(SlideInPanelService);

  experiment = computed(() => this.experimentDetailService.experimentDetail());

  readonly BUTTONS = BUTTONS;

  readonly EXPERIMENT_STATUS_DECORATION_MAP = EXPERIMENT_STATUS_DECORATION_MAP;

  onPerformAction(action: Action): void {
    switch (action) {
      case Action.COMPLETE:
        this.runWorkflow('complete', 'Experiment marked as completed');
        break;

      case Action.REOPEN:
        this.runWorkflow('reopen', 'Experiment reopened');
        break;

      case Action.CANCEL:
        this.runWorkflow('cancel', 'Experiment cancelled');
        break;

      case Action.COMPLETE_AND_SIGN:
        this.openSignDialog((templateId) =>
          this.runWorkflow('completeAndSubmit', 'Experiment completed and submitted for signature', {
            signatureTemplateId: templateId,
          }),
        );
        break;

      case Action.SUBMIT:
      case Action.RESUBMIT:
        this.openSignDialog((templateId) =>
          this.runWorkflow('submit', 'Experiment submitted for signature', {
            signatureTemplateId: templateId,
          }),
        );
        break;

      case Action.PRINT:
        this.downloadService
          .download('post', `/api/eln/experiments/${this.experiment().id}/print`, 'report.pdf')
          .subscribe({});
        this.notificationService.notify({
          type: NotificationType.Info,
          message: 'Starting report generation',
          isInline: false,
        });
        break;
    }
  }

  private runWorkflow(operation: string, successMessage: string, params?: Record<string, string>): void {
    this.experimentDetailService.executeWorkflow(operation, params).subscribe({
      next: () =>
        this.notificationService.notify({
          message: successMessage,
          type: NotificationType.Success,
          isInline: false,
        }),
    });
  }

  private openSignDialog(onConfirmed: (templateId: string) => void): void {
    this.dialog
      .open(SignDialogComponent)
      .afterClosed()
      .pipe(take(1))
      .subscribe((templateId: string | undefined) => {
        if (templateId) {
          onConfirmed(templateId);
        }
      });
  }

  openAddMemberDrawer(): void {
    const experimentId = this.experiment()?.id;
    if (!experimentId) return;

    this.slideInPanelService.open(ExperimentTeamDrawerComponent, {
      inputs: {
        experimentId: experimentId,
      },
    });
  }
}
