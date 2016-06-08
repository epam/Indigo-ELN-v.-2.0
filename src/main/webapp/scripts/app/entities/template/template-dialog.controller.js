angular.module('indigoeln').controller('TemplateDialogController',
    function ($scope, $stateParams, Template, $state, dragulaService, Components, pageInfo, TabManager) {
        $scope.components = Components;
        $scope.template = pageInfo.entity || {};
        $scope.template.templateContent = $scope.template.templateContent || [];

        var onSaveSuccess = function () {
            $state.go('template');
            $scope.isSaving = false;
            if (!$scope.template.id) {
                TabManager.closeTab('New Template');
            } else {
                TabManager.closeTab('Edit Template');
            }
        };

        var onSaveError = function () {
            $scope.isSaving = false;
        };

        $scope.save = function () {
            $scope.isSaving = true;
            if ($scope.template.id) {
                Template.update($scope.template, onSaveSuccess, onSaveError);
            } else {
                Template.save($scope.template, onSaveSuccess, onSaveError);
            }
        };

        $scope.cancel = function () {
            $state.go('template');
            if (!$scope.template.id) {
                TabManager.closeTab('New Template');
            } else {
                TabManager.closeTab('Edit Template');
            }
        };

        var hasComponent = function (id) {
            var component = _.chain($scope.template.templateContent).map(function (tc) {
                return tc.components;
            }).flatten().find(function (c) {
                return c.id === id;
            }).value();
            return !_.isUndefined(component);
        };

        dragulaService.options($scope, 'components', {
            //removeOnSpill: true,
            copy: function (el, source) {
                return source.classList.contains('palette');
            },
            accepts: function (el, target) {
                var componentId = angular.element(el).data('id');
                return !target.classList.contains('palette') && !hasComponent(componentId);
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
            });
        };
        if (!$scope.template.templateContent.length) {
            $scope.addTab();
        }

        $scope.removeTab = function (tab) {
            $scope.template.templateContent = _.without($scope.template.templateContent, tab);
        };

        $scope.removeComponent = function (tab, component) {
            tab.components = _.without(tab.components, component);
        };

    });
