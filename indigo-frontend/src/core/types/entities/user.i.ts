export interface UsersResponse {
  pageNo: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  items: User[];
}

export interface User {
  id: string;
  createdBy: UserMetadata;
  createdAt: string;
  modifiedBy: UserMetadata;
  modifiedAt: string;
  username: string;
  firstName: string | null;
  lastName: string | null;
  displayName: string;
  roles: Role[];
}

export interface UserMetadata {
  id: string;
  username: string;
  displayName: string;
}

export interface Role {
  id: string;
  name: string;
}