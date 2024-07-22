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

/*
var roles = {
    PROJECT_READER: 'PROJECT_READER',
    PROJECT_CREATOR: 'PROJECT_CREATOR',
    NOTEBOOK_READER: 'NOTEBOOK_READER',
    NOTEBOOK_CREATOR: 'NOTEBOOK_CREATOR',
    EXPERIMENT_READER: 'EXPERIMENT_READER',
    EXPERIMENT_CREATOR: 'EXPERIMENT_CREATOR',
    CONTENT_EDITOR: 'CONTENT_EDITOR',
    READ_ENTITY: 'READ_ENTITY',
    CREATE_SUB_ENTITY: 'CREATE_SUB_ENTITY',
    UPDATE_ENTITY: 'UPDATE_ENTITY',
    GLOBAL_SEARCH: 'GLOBAL_SEARCH',
    USER_EDITOR: 'USER_EDITOR',
    ROLE_EDITOR: 'ROLE_EDITOR',
    TEMPLATE_EDITOR: 'TEMPLATE_EDITOR',
    DICTIONARY_EDITOR: 'DICTIONARY_EDITOR'
};
*/

var userPermissions = {
    VIEWER: {
        id: 'VIEWER', name: 'VIEWER (read entity)'
    },
    USER: {
        id: 'USER', name: 'USER (read entity, create sub-entities)'
    },
    OWNER: {
        id: 'OWNER', name: 'OWNER (read/update entity, create sub-entities)'
    }
};

describe('service: permissionService', function() {
    var permissionService;

    beforeEach(angular.mock.module('indigoeln'));

    beforeEach(angular.mock.inject(function(_permissionService_) {
        permissionService = _permissionService_;
    }));

    it('should be defined', function() {
        expect(permissionService).toBeDefined();
    });

    describe('getPossiblePermissionViews', function() {
        it('project', function() {
            expect(permissionService.getPossiblePermissionViews('project')).toEqual([
                userPermissions.VIEWER,
                userPermissions.USER,
                userPermissions.OWNER
            ]);
        });

        it('notebook', function() {
            expect(permissionService.getPossiblePermissionViews('notebook')).toEqual([
                userPermissions.VIEWER,
                userPermissions.USER,
                userPermissions.OWNER
            ]);
        });

        it('experiment', function() {
            expect(permissionService.getPossiblePermissionViews('experiment')).toEqual([
                userPermissions.VIEWER,
                userPermissions.OWNER
            ]);
        });
    });
});
