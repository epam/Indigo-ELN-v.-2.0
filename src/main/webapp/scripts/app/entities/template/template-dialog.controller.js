'use strict';

angular.module('indigoeln').controller('TemplateDialogController',
    function ($scope, $stateParams, entity, Template, $state, dragulaService, Components) {
        $scope.components = Components;
        dragulaService.options($scope, 'template', {
            revertOnSpill: true
        });
        $scope.template = entity || {};
        $scope.template.templateContent = $scope.template.templateContent || [];
        $scope.load = function (id) {
            Template.get({id: id}, function (result) {
                $scope.template = result;
            });
        };
        $scope.components = _.filter($scope.components, function (component) {
            return !_.find($scope.template.templateContent, function (item) {
                return component.id == item.id;
            })
        });

        var onSaveSuccess = function (result) {
            $state.go('template');
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
    });
