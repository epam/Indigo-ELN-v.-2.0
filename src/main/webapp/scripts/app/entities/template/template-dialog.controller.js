'use strict';

angular.module('indigoeln').controller('TemplateDialogController',
    function ($scope, $stateParams, entity, Template, $state, $builder, $validator) {

        $scope.template = entity || {};
        $builder.forms['default'].length = 0;
        _.each($scope.template.templateContent, function (item) {
            if (item) {
                $builder.addFormObject("default", item);
            }
        });
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
            $scope.template.templateContent = $builder.forms['default'];
            if ($scope.template.id != null) {
                Template.update($scope.template, onSaveSuccess, onSaveError);
            } else {
                Template.save($scope.template, onSaveSuccess, onSaveError);
            }
        };

        $scope.submit = function () {
            return $validator.validate($scope, 'default').success(function () {
                return console.log('success');
            }).error(function () {
                return console.log('error');
            });
        };
    });
