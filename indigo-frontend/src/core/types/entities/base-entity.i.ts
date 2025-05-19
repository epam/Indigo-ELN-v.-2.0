export interface BaseEntity {
  id: string;
  createdBy?: EdBy;
  createdAt?: Date;
  modifiedBy?: EdBy;
  modifiedAt?: Date;
}

export interface EdBy {
  id: string;
  username: string;
  displayName: string;
}
