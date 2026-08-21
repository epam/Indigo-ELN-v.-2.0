export type AccessLevel = 'NONE' | 'IMPLICIT_VIEW' | 'VIEW' | 'EDIT' | 'ADMIN' | 'AUTHOR';

export interface UserRef {
  username: string;
  displayName: string;
}

export interface ACLEntry {
  username: string;
  displayName: string;
  level: AccessLevel;
  inherited: boolean;
}

export type UUID = string;

export interface BaseDTO {
  id: UUID;
  createdBy: UserRef;
  createdAt: string;
  modifiedBy: UserRef;
  modifiedAt: string;
}

export interface Page<T> {
  pageNo: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  items: T[];
}
