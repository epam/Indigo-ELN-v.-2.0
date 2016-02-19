'use strict';

angular.module('indigoeln')
    .controller('EntitiesController', function ($scope, entities, $stateParams, EntitiesBrowser, $state) {
        $scope.entityId = EntitiesBrowser.compactIds($stateParams);
        $scope.entities = entities;
        $scope.closeTab = function (fullId, entityId) {
            EntitiesBrowser.close(fullId, entityId)
        };
        $scope.goToTab = function (fullId) {
            EntitiesBrowser.goToTab(fullId)
        };
    });
