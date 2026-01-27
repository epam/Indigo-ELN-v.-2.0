import { normalizeLabel } from "../utils/string.util";

export enum AclLevel {
  ADMIN = 'ADMIN',
  CAN_EDIT = 'CAN_EDIT',
  CAN_VIEW = 'CAN_VIEW',
  IMPLICIT_VIEW = 'IMPLICIT_VIEW',
  REMOVE_FROM_PROJECT = 'REMOVE_FROM_PROJECT',
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