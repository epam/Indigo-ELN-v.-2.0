'use strict';

angular.module('indigoeln').controller('TemplateDialogController',
    function ($scope, $stateParams, entity, Template, $state) {

        $scope.template = entity || {};
        $scope.load = function (id) {
            Template.get({id: id}, function (result) {
                $scope.template = result;
            });
        };

        var onSaveSuccess = function (result) {
            $state.go('template');
            $scope.isSaving = false;
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
        };

        $scope.save = function () {
            $scope.isSaving = true;
            //$scope.template.templateContent = $builder.forms['default'];
            if ($scope.template.id != null) {
                Template.update($scope.template, onSaveSuccess, onSaveError);
            } else {
                Template.save($scope.template, onSaveSuccess, onSaveError);
            }
        };
    });
