import { AclLevel } from '@/core/enums/acl-levels.enum';

export interface ACLEntry {
  username: string;
  displayName: string;
  level: AclLevel;
  inherited: boolean;
}

export interface ACLUpdate {
  username: string;
  level: AclLevel;
  deleteNested?: boolean;
}
