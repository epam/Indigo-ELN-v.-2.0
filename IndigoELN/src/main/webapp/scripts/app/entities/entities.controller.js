'use strict';

angular.module('indigoeln')
    .controller('EntitiesController', function ($scope, data, $stateParams, EntitiesBrowser) {
        $scope.entityId = EntitiesBrowser.compactIds($stateParams);
        $scope.entities = data.entities;
        $scope.onCloseTabClick = function (fullId, entityId) {
            EntitiesBrowser.close(fullId, entityId);
            EntitiesBrowser.getTabs().then(function (tabs) {
                $scope.entities = tabs;
            })
        };
        $scope.onTabClick = function (fullId) {
            EntitiesBrowser.goToTab(fullId)
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
                if (kind == 'experiment') {
                    EntitiesBrowser.resolveFromCache({
                        projectId: params.projectId,
                        notebookId: params.notebookId
                    }).then(function (notebook) {
                        item.title = notebook.name ? notebook.name + '-' + item.name : item.name;
                    })
                } else {
                    item.title = item.name;
                }
            })
        });
    });
