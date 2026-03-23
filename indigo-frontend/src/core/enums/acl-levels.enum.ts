export enum AclLevel {
  ADMIN = 'ADMIN',
  EDIT = 'EDIT',
  VIEW = 'VIEW',
  IMPLICIT_VIEW = 'IMPLICIT_VIEW',
  NONE = 'NONE',
  AUTHOR = 'AUTHOR',
}

export const INMUTABLE_ACL_LEVELS = [AclLevel.AUTHOR]

export const isInmutableLevel = (level: AclLevel): boolean => {
  return INMUTABLE_ACL_LEVELS.includes(level);
}

export const ACL_LEVEL_LABELS: Record<AclLevel, string> = {
  [AclLevel.ADMIN]: 'Admin',
  [AclLevel.EDIT]: 'Can Edit',
  [AclLevel.VIEW]: 'Can View',
  [AclLevel.IMPLICIT_VIEW]: 'Implicit View',
  [AclLevel.NONE]: 'None',
  [AclLevel.AUTHOR]: 'Author',
};

export const ELIGIBLE_ACL_LEVELS = Object.values(AclLevel)
  .filter((value) => !isInmutableLevel(value))
  .map((value) => ({
    label: ACL_LEVEL_LABELS[value],
    value,
  }));