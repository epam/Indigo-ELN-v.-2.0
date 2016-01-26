'use strict';

angular.module('indigoeln')
    .controller('TemplateDeleteController', function ($scope, $uibModalInstance, entity, Template) {

        $scope.template = entity;
        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };
        $scope.confirmDelete = function (id) {
            Template.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        };

    });
