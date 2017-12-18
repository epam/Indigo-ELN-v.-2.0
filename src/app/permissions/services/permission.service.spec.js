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
            var projectOwner = {
                authorities: [
                    roles.PROJECT_READER,
                    roles.PROJECT_CREATOR,
                    roles.NOTEBOOK_READER,
                    roles.NOTEBOOK_CREATOR
                ]
            };

            var projectUser = {
                authorities: [roles.PROJECT_READER, roles.NOTEBOOK_READER, roles.NOTEBOOK_CREATOR]
            };
            var projectViewer = {
                authorities: [roles.PROJECT_READER]
            };

            expect(permissionService.getPossiblePermissionViews(projectOwner, 'project')).toEqual([
                userPermissions.OWNER,
                userPermissions.USER,
                userPermissions.VIEWER
            ]);
            expect(permissionService.getPossiblePermissionViews(projectUser, 'project')).toEqual([
                userPermissions.USER,
                userPermissions.VIEWER
            ]);
            expect(permissionService.getPossiblePermissionViews(projectViewer, 'project')).toEqual([
                userPermissions.VIEWER
            ]);
        });

        it('notebook', function() {
            var owner = {
                authorities: [
                    roles.NOTEBOOK_READER,
                    roles.NOTEBOOK_CREATOR,
                    roles.EXPERIMENT_READER,
                    roles.EXPERIMENT_CREATOR
                ]
            };

            var user = {authorities: [roles.NOTEBOOK_READER, roles.EXPERIMENT_READER, roles.EXPERIMENT_CREATOR]};

            var viewer = {authorities: [roles.NOTEBOOK_READER]};

            expect(permissionService.getPossiblePermissionViews(owner, 'notebook')).toEqual([
                userPermissions.OWNER,
                userPermissions.USER,
                userPermissions.VIEWER
            ]);
            expect(permissionService.getPossiblePermissionViews(user, 'notebook')).toEqual([
                userPermissions.USER,
                userPermissions.VIEWER
            ]);
            expect(permissionService.getPossiblePermissionViews(viewer, 'notebook')).toEqual([
                userPermissions.VIEWER
            ]);
        });

        it('experiment', function() {
            var owner = {
                authorities: [roles.EXPERIMENT_READER, roles.EXPERIMENT_CREATOR]
            };

            var viewer = {authorities: [roles.EXPERIMENT_READER]};

            expect(permissionService.getPossiblePermissionViews(owner, 'experiment')).toEqual([
                userPermissions.OWNER,
                userPermissions.VIEWER
            ]);

            expect(permissionService.getPossiblePermissionViews(viewer, 'experiment')).toEqual([
                userPermissions.VIEWER
            ]);
        });
    });
});
