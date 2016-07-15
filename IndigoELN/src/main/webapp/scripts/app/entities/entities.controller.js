angular.module('indigoeln')
    .controller('EntitiesController', function ($scope, EntitiesBrowser, $rootScope, $q, $location) {
        var initParams = $location.path().match(/\d+/g);

        function updateTabs(toParams) {
            resolveTabs(toParams).then(function (data) {
                $scope.entities = data.entities;
                $scope.entityId = EntitiesBrowser.compactIds(toParams);
                $rootScope.$broadcast('entities-updated', {entities: $scope.entities, entityId: $scope.entityId});
            });
        }

        updateTabs({
            projectId: initParams[0],
            notebookId: initParams[1],
            experimentId: initParams[2]
        });

        $scope.$on('entity-clicked', function (event, fullId) {
            $scope.onTabClick(fullId);
        });
        $scope.$on('entity-closing', function (event, data) {
            $scope.onCloseTabClick(data.fullId, data.entityId);
        });
        $scope.$on('entity-close-all', function () {
            EntitiesBrowser.closeAll().then(function () {
                EntitiesBrowser.getTabs().then(function (tabs) {
                    $scope.entities = tabs;
                });
            });
        });
        $scope.onCloseTabClick = function (fullId, entityId) {
            EntitiesBrowser.close(fullId, entityId);
            EntitiesBrowser.getTabs().then(function (tabs) {
                $scope.entities = tabs;
            });
        };
        $scope.onTabClick = function (fullId) {
            EntitiesBrowser.goToTab(fullId);
        };
        $scope.getKind = function (fullId) {
            return EntitiesBrowser.getKind(EntitiesBrowser.expandIds(fullId));
        };
        var unsubscribe = $scope.$watch(function () {
            return _.map($scope.entities, _.iteratee('name')).join('-');
        }, function () {
            _.each($scope.entities, function (item) {
                var params = EntitiesBrowser.expandIds(item.fullId);
                var kind = EntitiesBrowser.getKind(params);
                if (kind === 'experiment') {
                    EntitiesBrowser.resolveFromCache({
                        projectId: params.projectId,
                        notebookId: params.notebookId
                    }).then(function (notebook) {
                        item.$$title = notebook.name ? notebook.name + '-' + item.name : item.name;
                        if (item.experimentVersion > 1 || !item.lastVersion) {
                            item.$$title += ' v' + item.experimentVersion;
                        }
                    });
                } else {
                    item.$$title = item.name;
                }
            });
        });

        $scope.$on('$destroy', function () {
            unsubscribe();
        });

        $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams) {
            updateTabs(toParams);
        });
        $rootScope.$on('updateTabs', function (event, toParams) {
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
