export interface BaseEntity {
  id: number;
  createdBy: EdBy;
  createdAt: Date;
  modifiedBy: EdBy;
  modifiedAt: Date;
}

export interface EdBy {
  id: number;
  username: string;
  displayName: string;
}
