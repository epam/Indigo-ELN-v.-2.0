import { UUID } from '@core/types/entities/experiments/experiment-shared.i';
import { UserRef } from '@core/types/entities/user.i';

export enum DocumentStatus {
  SUBMITTED = 'SUBMITTED',
  SIGNING = 'SIGNING',
  SIGNED = 'SIGNED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED',
}

export enum SignatureReason {
  AUTHOR = 'AUTHOR',
  WITNESS = 'WITNESS',
}

export enum SignatureStatus {
  WAITING = 'WAITING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

export interface Document {
  id: UUID;
  name: string;
  status: DocumentStatus;
  createdDate: Date;
  lastModifiedDate: Date;
  author: UserRef;
  signatures: DocumentSignature[];
}

export interface DocumentSignature {
  user: UserRef;
  reason: SignatureReason;
  status: SignatureStatus;
  comment: string;
  canSignOrReject: boolean;
}
