export enum NotificationType {
  Success = 'success',
  Warning = 'warning',
  Error = 'error',
  Info = 'info',
}

export interface NotificationParams {
  message: string;
  type: NotificationType;
  isInline: boolean;
}
