'use strict';

angular.module('indigoeln').controller('TemplateDialogController',
    ['$scope', '$stateParams', '$uibModalInstance', 'entity', 'Template',
        function ($scope, $stateParams, $uibModalInstance, entity, Template) {

            $scope.template = entity;
            $scope.load = function (id) {
                Template.get({id: id}, function (result) {
                    $scope.template = result;
                });
            };

            var onSaveSuccess = function (result) {
                $scope.$emit('indigoeln:templateUpdate', result);
                $uibModalInstance.close(result);
                $scope.isSaving = false;
            };

            var onSaveError = function (result) {
                $scope.isSaving = false;
            };

            $scope.save = function () {
                $scope.isSaving = true;
                if ($scope.template.id != null) {
                    Template.update($scope.template, onSaveSuccess, onSaveError);
                } else {
                    Template.save($scope.template, onSaveSuccess, onSaveError);
                }
            };

            $scope.clear = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }]);
