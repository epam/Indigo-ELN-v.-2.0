import { normalizeLabel } from "../utils/string.util";

export enum AclLevel {
  NONE = 'NONE',
  IMPLICIT_VIEW = 'IMPLICIT_VIEW',
  VIEW = 'VIEW',
  EDIT = 'EDIT',
  ADMIN = 'ADMIN',
  AUTHOR = 'AUTHOR',
}

export const INMUTABLE_ACL_LEVELS = [AclLevel.AUTHOR]

export const isInmutableLevel = (level: AclLevel): boolean => {
  return INMUTABLE_ACL_LEVELS.includes(level);
}

export const ELIGIBLE_ACL_LEVELS = Object.values(AclLevel)
    .filter((value) => !isInmutableLevel(value))
    .map((value) => ({
      label: normalizeLabel(value),
      value
    }));