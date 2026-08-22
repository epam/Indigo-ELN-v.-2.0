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

export interface Attachment extends BaseDTO {
  name: string;
  size: number;
}

export interface Page<T> {
  pageNo: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  items: T[];
}

/** Sort and layout choices are the same on every entity list (projects, notebooks, experiments). */
export const SORT_ORDERS = ['EARLIEST', 'LATEST'] as const;
export type SortOrder = (typeof SORT_ORDERS)[number];

export const COLLECTION_VIEWS = ['grid', 'list'] as const;
export type CollectionView = (typeof COLLECTION_VIEWS)[number];
