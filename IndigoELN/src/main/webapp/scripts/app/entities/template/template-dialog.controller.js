'use strict';

angular.module('indigoeln').controller('TemplateDialogController',
    function ($scope, $stateParams, Template, $state, dragulaService, Components, pageInfo) {
        $scope.components = Components;
        $scope.template = pageInfo.entity || {};
        $scope.template.templateContent = $scope.template.templateContent || [];

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

        dragulaService.options($scope, 'components', {
            //removeOnSpill: true,
            copy: function (el, source) {
                return source.classList.contains('palette')
            },
            accepts: function (el, target) {
                return !target.classList.contains('palette')
            },
            moves: function (el, container, handle) {
                return !handle.classList.contains('no-draggable');
            }
        });

        dragulaService.options($scope, 'tabs', {
            //removeOnSpill: true,
            moves: function (el, container, handle) {
                return !handle.classList.contains('draggable-component') && !handle.classList.contains('no-draggable');
            }
        });

        $scope.addTab = function () {
            $scope.template.templateContent.push({
                name: 'Tab ' + ($scope.template.templateContent.length + 1),
                components: []
            })
        };
        if (!$scope.template.templateContent.length) {
            $scope.addTab();
        }

        $scope.removeTab = function (tab) {
            $scope.template.templateContent = _.without($scope.template.templateContent, tab);
        };

        $scope.removeComponent = function (tab, component) {
            tab.components = _.without(tab.components, component);
        }

    });
