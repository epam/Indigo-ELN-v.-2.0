'use strict';

angular.module('indigoeln')
    .controller('role-managementDeleteController', function ($scope, $uibModalInstance, Role, entity) {

        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.confirmDelete = function () {
            Role.delete({id: entity.id},
                function () {
                    $uibModalInstance.close(true);
                },
                function () {
                    $uibModalInstance.close(false);
                });
        };
    });