'use strict';

angular.module('indigoeln')
    .controller('EntitiesController', function ($scope, EntitiesBrowser, $rootScope, $q, $location) {
        //todo: refactoring
        var initParams = $location.path().match(/\d+/g);

        updateTabs({
            projectId: initParams[0],
            notebookId: initParams[1],
            experimentId: initParams[2]
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
        $scope.$watch(function () {
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
                        item.title = notebook.name ? notebook.name + '-' + item.name : item.name;
                    });
                } else {
                    item.title = item.name;
                }
            });
        });

        function updateTabs(toParams) {
            resolveTabs(toParams).then(function (data) {
                $scope.entities = data.entities;
                $scope.entityId = EntitiesBrowser.compactIds(toParams);
            });
        }

        $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams, fromState, fromParams) {
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
