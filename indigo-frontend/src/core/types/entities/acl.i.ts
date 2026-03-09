import { AclLevel } from "@/core/enums/acl-levels.enum";

export interface ProjectAcl {
  userId: string;
  username: string;
  displayName: string;
  level: AclLevel;
  inherited: boolean;
}

export interface UserSuggestion {
  id: string;
  username: string;
  displayName: string;
}

export interface ProjectAclUpdate {
  userID: string;
  level: AclLevel;
  deleteNested?: boolean;
}

export interface NestedAccessItem {
  entityType: string;
  entityId: string;
  entityName: string;
  userId: string;
  displayName: string;
  level: AclLevel;
}
