import { UserRef } from '@core/types/entities/user.i';

export interface BaseEntity {
  id: string;
  createdBy?: UserRef;
  createdAt?: Date;
  modifiedBy?: UserRef;
  modifiedAt?: Date;
}

export interface BackendError {
  path?: string;
  message: string;
}
