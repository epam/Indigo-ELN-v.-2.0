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

/**
 * What every paged entity list is filtered by. One type rather than one per entity: the
 * projects and notebooks resources declare the identical query params, and `ActionBar` — the
 * single control that sets all three — is entity-agnostic too.
 */
export interface CollectionFilters {
  search: string;
  sort: SortOrder;
  createdByMe: boolean;
}

/**
 * Access levels as the UI speaks about them, ported from indigo-frontend's
 * `acl-levels.enum.ts`. `AUTHOR` is the creator's own entry: the backend never lets it change,
 * so it is shown but never offered.
 */
export const ACL_LEVEL_LABELS: Record<AccessLevel, string> = {
  ADMIN: 'Admin',
  EDIT: 'Can Edit',
  VIEW: 'Can View',
  IMPLICIT_VIEW: 'Limited View',
  NONE: 'None',
  AUTHOR: 'Author',
};

export function isImmutableLevel(level: AccessLevel): boolean {
  return level === 'AUTHOR';
}

export const ELIGIBLE_ACL_LEVELS = (Object.keys(ACL_LEVEL_LABELS) as AccessLevel[]).filter(
  (level) => !isImmutableLevel(level),
);

/** One entry of the `POST /access` body. The backend upserts per entry, so send only what changed. */
export interface AccessForm {
  username: string;
  level: AccessLevel;
  deleteNested?: boolean;
}
