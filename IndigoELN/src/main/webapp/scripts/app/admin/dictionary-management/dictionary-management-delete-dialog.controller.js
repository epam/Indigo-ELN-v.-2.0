'use strict';

angular.module('indigoeln')
    .controller('DictionaryManagementDeleteController', function ($scope, $uibModalInstance, Dictionary, entity) {

        $scope.dismiss = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.confirmDelete = function () {
            Dictionary.delete({id: entity.id},
                function () {
                    $uibModalInstance.close(true);
                },
                function () {
                    $uibModalInstance.close(false);
                });
        };
    });