'use strict';

angular.module('indigoeln')
    .controller('TemplateDeleteController', function ($scope, $uibModalInstance, pageInfo, Template) {

        $scope.template = pageInfo.entity;
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
