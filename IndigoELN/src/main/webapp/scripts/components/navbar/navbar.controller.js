angular.module('indigoeln')
    .controller('NavbarController', function ($scope, $state, $rootScope, $q, Principal, Auth, EntitiesBrowser, EntitiesCache) {
        $scope.CONTENT_EDITOR = 'CONTENT_EDITOR';
        $scope.PROJECT_CREATOR = 'PROJECT_CREATOR';
        $scope.NOTEBOOK_CREATOR = 'NOTEBOOK_CREATOR';
        $scope.EXPERIMENT_CREATOR = 'EXPERIMENT_CREATOR';
        $scope.GLOBAL_SEARCH = 'GLOBAL_SEARCH';
        $scope.PROJECT_CREATORS = [$scope.CONTENT_EDITOR, $scope.PROJECT_CREATOR].join(',');
        $scope.NOTEBOOK_CREATORS = [$scope.CONTENT_EDITOR, $scope.NOTEBOOK_CREATOR].join(',');
        $scope.EXPERIMENT_CREATORS = [$scope.CONTENT_EDITOR, $scope.EXPERIMENT_CREATOR].join(',');
        $scope.ENTITY_CREATORS = [$scope.CONTENT_EDITOR, $scope.PROJECT_CREATOR, $scope.NOTEBOOK_CREATOR, $scope.EXPERIMENT_CREATOR].join(',');

        $scope.Principal = Principal;

        var userId =  Principal.getIdentity().id;
        $scope.entities = EntitiesBrowser.tabs[userId];
        $scope.activeTab = EntitiesBrowser.activeTab;


        $scope.onEntityClick = function (entity) {
            $rootScope.$broadcast('entity-clicked', entity);
        };
        $scope.onCloseEntityClick = function (entity) {
            $rootScope.$broadcast('entity-closing', entity);
        };
        $scope.onCloseAllClick = function () {
            $rootScope.$broadcast('entity-close-all');

        };

        $scope.openSearch = function () {
            $state.go('entities.search-panel')
        };

        $scope.logout = function () {
            var userId = Principal.getIdentity().id;
            Auth.logout();
            EntitiesCache.clearAll();
            $rootScope.$broadcast('user-logout', {id: userId});
            $state.go('login');
        };


        var unsubscribe = $scope.$watch(function () {
            return EntitiesBrowser.activeTab;
        }, function (value) {
            $scope.activeTab = value;
        });

        $scope.$on('$destroy', function () {
            unsubscribe();
        });

    });