import { AclLevel } from '@/core/enums/acl-levels.enum';

export interface ProjectAcl {
  username: string;
  displayName: string;
  level: AclLevel;
  inherited: boolean;
}

export interface ProjectAclUpdate {
  username: string;
  level: AclLevel;
}
