/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

var roles = require('../permission-roles.json');

/* @ngInject */
function permissionService($q, principalService, userRemovableFromProjectService, userRemovableFromNotebookService,
                           permissionsConstant, userPermissions) {
    var accessList;
    var author;
    var entity;
    var entityId;
    var parentId;

    var VIEWER = [roles.READ_ENTITY];
    var USER = [roles.READ_ENTITY, roles.CREATE_SUB_ENTITY];
    var OWNER = [roles.READ_ENTITY, roles.CREATE_SUB_ENTITY, roles.UPDATE_ENTITY];

    return {
        expandPermission: expandPermission,
        hasPermission: hasPermission,
        getAuthorAccessList: getAuthorAccessList,
        getAccessList: getAccessList,
        setAccessList: setAccessList,
        getAuthor: getAuthor,
        setAuthor: setAuthor,
        getEntity: getEntity,
        setEntity: setEntity,
        getEntityId: getEntityId,
        setEntityId: setEntityId,
        getParentId: getParentId,
        setParentId: setParentId,
        hasAuthorityForProjectPermission: hasAuthorityForProjectPermission,
        hasAuthorityForNotebookPermission: hasAuthorityForNotebookPermission,
        hasAuthorityForExperimentPermission: hasAuthorityForExperimentPermission,
        hasAuthorityForPermission: hasAuthorityForPermission,
        isUserRemovableFromAccessList: isUserRemovableFromAccessList,
        setProject: setProject,
        setNotebook: setNotebook,
        setExperiment: setExperiment,
        isContentEditor: isContentEditor,
        isAuthor: isAuthor,
        getPossiblePermissionViews: getPossiblePermissionViews
    };

    /**
     * Returns ordered views by a capacity role.
     * @return {Array}
     */
    function getPossiblePermissionViews(entityType) {
        var permissionsList = _.map(userPermissions);

        if (entityType === 'experiment') {
            _.pull(permissionsList, userPermissions.USER);
        }

        return permissionsList;
    }

    function isContentEditor(user) {
        return _.includes(user.authorities, roles.CONTENT_EDITOR);
    }

    function isAuthor(user) {
        return user.login === _.get(getAuthor(), 'login');
    }

    function expandPermission(list) {
        _.each(list, function(item) {
            if (item.permissionView === userPermissions.OWNER.id) {
                item.permissions = OWNER;
            } else if (item.permissionView === userPermissions.USER.id) {
                item.permissions = USER;
            } else {
                item.permissions = VIEWER;
            }
        });

        return list;
    }

    function hasPermission(permission) {
        if (!accessList) {
            return false;
        }

        var userId = principalService.getIdentity().id;

        return _.some(accessList, function(item) {
            return item.user.id === userId && _.includes(item.permissions, permission);
        });
    }

    function getAuthorAccessList(entityAuthor, projectAuthor) {
        var experimentCreatorPermissions = {
            user: entityAuthor,
            permissions: [roles.READ_ENTITY, roles.CREATE_SUB_ENTITY, roles.UPDATE_ENTITY],
            permissionView: userPermissions.OWNER.id
        };
        if (projectAuthor && projectAuthor.id !== entityAuthor.id) {
            var projectCreatorPermissions = {
                user: projectAuthor,
                permissions: [roles.READ_ENTITY],
                permissionView: userPermissions.VIEWER.id
            };

            return [experimentCreatorPermissions, projectCreatorPermissions];
        }

        return [experimentCreatorPermissions];
    }

    function getAccessList() {
        return accessList;
    }

    function setAccessList(newAccessList) {
        accessList = newAccessList;
    }

    function getAuthor() {
        return author;
    }

    function setAuthor(newAuthor) {
        author = newAuthor;
    }

    function getEntity() {
        return entity;
    }

    function setNotebook(notebook, projectId) {
        setEntity('Notebook');
        setEntityId(notebook.id);
        setParentId(projectId);
        setAuthor(notebook.author);
        setAccessList(notebook.accessList);
    }

    function setExperiment(experiment) {
        setEntity('Experiment');
        setAuthor(experiment.author);
        setAccessList(experiment.accessList);
    }

    function setProject(project) {
        setEntity('Project');
        setEntityId(project.id);
        setAuthor(project.author);
        setAccessList(project.accessList);
    }

    function setEntity(newEntity) {
        entity = newEntity;
    }

    function getEntityId() {
        return entityId;
    }

    function setEntityId(newEntityId) {
        entityId = newEntityId;
    }

    function getParentId() {
        return parentId;
    }

    function setParentId(newParentId) {
        parentId = newParentId;
    }

    function hasAllAuthorities(authoritiesFrom, authoritiesTo) {
        return _.every(authoritiesTo, function(authority) {
            return _.includes(authoritiesFrom, authority);
        });
    }

    function hasAuthorityForProjectPermission(member, permission) {
        if (permission === userPermissions.OWNER.id) {
            return hasAllAuthorities(member.user.authorities, permissionsConstant.PROJECT_OWNER_AUTHORITY_SET);
        } else if (permission === userPermissions.USER.id) {
            return hasAllAuthorities(member.user.authorities, permissionsConstant.PROJECT_USER_AUTHORITY_SET);
        } else if (permission === userPermissions.VIEWER.id) {
            return hasAllAuthorities(member.user.authorities, permissionsConstant.PROJECT_VIEWER_AUTHORITY_SET);
        }
    }

    function hasAuthorityForNotebookPermission(member, permission) {
        var notebookOwnerAuthoritySet = permissionsConstant.NOTEBOOK_OWNER_AUTHORITY_SET;
        var notebookUserAuthoritySet = permissionsConstant.NOTEBOOK_USER_AUTHORITY_SET;
        var notebookViewerAuthoritySet = permissionsConstant.NOTEBOOK_VIEWER_AUTHORITY_SET;

        if (permission === userPermissions.OWNER.id) {
            return _.every(notebookOwnerAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        } else if (permission === userPermissions.USER.id) {
            return _.every(notebookUserAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        } else if (permission === userPermissions.VIEWER.id) {
            return _.every(notebookViewerAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        }
    }

    function hasAuthorityForExperimentPermission(member, permission) {
        var experimentOwnerAuthoritySet = permissionsConstant.EXPERIMENT_OWNER_AUTHORITY_SET;
        var experimentViewerAuthoritySet = permissionsConstant.EXPERIMENT_VIEWER_AUTHORITY_SET;

        if (permission === userPermissions.OWNER.id) {
            return _.every(experimentOwnerAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        } else if (permission === userPermissions.VIEWER.id) {
            return _.every(experimentViewerAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        }
    }

    function hasAuthorityForPermission(member, permission) {
        if (_.includes(member.user.authorities, roles.CONTENT_EDITOR)) {
            return true;
        }
        if (entity === 'Project') {
            return hasAuthorityForProjectPermission(member, permission);
        } else if (entity === 'Notebook') {
            return hasAuthorityForNotebookPermission(member, permission);
        } else if (entity === 'Experiment') {
            return hasAuthorityForExperimentPermission(member, permission);
        }
    }

    function isUserRemovableFromAccessList(member) {
        if (entity === 'Experiment' || !entityId) {
            return $q.resolve(true);
        }

        if (entity === 'Project') {
            return userRemovableFromProjectService.get({
                projectId: entityId,
                userId: member.user.id
            })
                .$promise
                .then(isRemovable);
        } else if (entity === 'Notebook') {
            return userRemovableFromNotebookService.get({
                projectId: parentId,
                notebookId: entityId,
                userId: member.user.id
            })
                .$promise
                .then(isRemovable);
        }

        return $q.reject();
    }

    function isRemovable(result) {
        return result.isUserRemovable ? $q.resolve() : $q.reject();
    }
}

module.exports = permissionService;
