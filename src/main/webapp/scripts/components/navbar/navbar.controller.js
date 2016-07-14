angular.module('indigoeln')
    .controller('NavbarController', function ($scope, $state, $rootScope, $q, Principal, Auth, EntitiesBrowser) {
        $scope.CONTENT_EDITOR = 'CONTENT_EDITOR';
        $scope.PROJECT_CREATOR = 'PROJECT_CREATOR';
        $scope.NOTEBOOK_CREATOR = 'NOTEBOOK_CREATOR';
        $scope.EXPERIMENT_CREATOR = 'EXPERIMENT_CREATOR';
        $scope.PROJECT_CREATORS = [$scope.CONTENT_EDITOR, $scope.PROJECT_CREATOR].join(',');
        $scope.NOTEBOOK_CREATORS = [$scope.CONTENT_EDITOR, $scope.NOTEBOOK_CREATOR].join(',');
        $scope.EXPERIMENT_CREATORS = [$scope.CONTENT_EDITOR, $scope.EXPERIMENT_CREATOR].join(',');
        $scope.ENTITY_CREATORS = [$scope.CONTENT_EDITOR, $scope.PROJECT_CREATOR, $scope.NOTEBOOK_CREATOR, $scope.EXPERIMENT_CREATOR].join(',');

        $scope.Principal = Principal;
        EntitiesBrowser.getTabs().then(function (entities) {
            $scope.entities = entities;
        });
        $scope.$on('entities-updated', function (event, data) {
            $scope.entities = data.entities;
            $scope.entityId = data.entityId;
        });
        $scope.getKind = function (fullId) {
            return EntitiesBrowser.getKind(EntitiesBrowser.expandIds(fullId));
        };
        $scope.onEntityClick = function (fullId) {
            $rootScope.$broadcast('entity-clicked', fullId);
        };
        $scope.onCloseEntityClick = function (fullId, entityId) {
            $rootScope.$broadcast('entity-closing', {fullId: fullId, entityId: entityId});
        };

        $scope.onCloseAllClick = function () {
            EntitiesBrowser.closeAll();
        };
        $scope.onCloseTabClick = function (fullId, entityId) {
            EntitiesBrowser.close(fullId, entityId);
            EntitiesBrowser.getTabs().then(function (tabs) {
                $scope.entities = tabs;
            });
        };

        $scope.logout = function () {
            var userId = Principal.getIdentity().id;
            Auth.logout();
            $rootScope.$broadcast('user-logout', {id: userId});
            $state.go('login');
        };
    });