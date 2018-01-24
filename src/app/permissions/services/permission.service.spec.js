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
