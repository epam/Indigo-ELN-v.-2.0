angular.module('indigoeln').controller('EntitiesToSaveController',
    function ($scope, $rootScope, $uibModalInstance, EntitiesBrowser, data) {
        $scope.entities = _.map(data, function (entity) {
            entity.$$saveEntity = true;
            return entity;
        });

        $scope.discardAll = function () {
            _.each($scope.entities, function (entity) {
                entity.$$saveEntity = false;
            });
        };

        $scope.getKind = function (fullId) {
            return EntitiesBrowser.getKind(EntitiesBrowser.expandIds(fullId));
        };

        $scope.save = function () {
            var entitiesToSave = _.filter($scope.entities, function (entity) {
                return entity.$$saveEntity;
            });
            $uibModalInstance.close(entitiesToSave);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });
