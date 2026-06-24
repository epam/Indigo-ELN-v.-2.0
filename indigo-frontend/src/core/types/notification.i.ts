export enum NotificationType {
  Success = 'success',
  Warning = 'warning',
  Error = 'error',
  Info = 'info',
}

export interface NotificationAction {
  label: string;
  callback: () => void;
}

export interface NotificationParams {
  message: string;
  type: NotificationType;
  isInline: boolean;
  action?: NotificationAction;
}
