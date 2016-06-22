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

        EntitiesBrowser.getTabs().then(function (tabs) {
            $scope.entities = tabs;
        });

        $scope.getKind = function (fullId) {
            return EntitiesBrowser.getKind(EntitiesBrowser.expandIds(fullId));
        };
        $scope.onEntityClick = function (fullId) {
            EntitiesBrowser.goToTab(fullId);
        };

        $scope.logout = function () {
            var userId = Principal.getIdentity().id;
            Auth.logout();
            $rootScope.$broadcast('user-logout', {id: userId});
            $state.go('login');
        };

        function updateTabs(toParams) {
            resolveTabs(toParams).then(function (data) {
                $scope.entities = data.entities;
                $scope.entityId = EntitiesBrowser.compactIds(toParams);
            });
        }

        $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams) {
            updateTabs(toParams);
        });

        function resolveTabs($stateParams) {
            var params = {
                projectId: $stateParams.projectId,
                notebookId: $stateParams.notebookId,
                experimentId: $stateParams.experimentId
            };
            var deferred = $q.defer();
            EntitiesBrowser
                .resolveTabs(params)
                .then(function (tabs) {
                    var kind = EntitiesBrowser.getKind(params);
                    if (kind === 'experiment') {
                        EntitiesBrowser.resolveFromCache({
                            projectId: params.projectId,
                            notebookId: params.notebookId
                        }).then(function () {
                            deferred.resolve({
                                entities: tabs
                            });
                        });
                    } else {
                        deferred.resolve({
                            entities: tabs
                        });
                    }
                });
            return deferred.promise;
        }
    });