import { AclLevel } from "@/core/enums/acl-levels.enum";

export interface ProjectAcl {
  userId: string;
  username: string;
  displayName: string;
  level: AclLevel;
  inherited: boolean;
}
