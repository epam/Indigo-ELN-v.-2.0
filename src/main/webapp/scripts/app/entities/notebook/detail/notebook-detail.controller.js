'use strict';

angular.module('indigoeln')
    .controller('NotebookDetailController', function ($scope, $state, $stateParams, $uibModal, Template, data) {
        $scope.notebook = data;
        $scope.description = '';

        $scope.newExperiment = function () {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/experiment/dialog/new-experiment-dialog.html',
                controller: 'NewExperimentDialogController',
                scope: $scope,
                resolve: {
                    templates: function(Template, $stateParams) {
                        return Template.query({}, {
                            accessList: [] //TODO add access list [{userId: 'userId', permissions: 'RERSCSUE'}, {...}]
                        }).$promise;
                    },
                    notebook: function() {
                        return {
                            notebookId: $scope.notebook.id,
                            notebookName: $scope.notebook.name,
                            projectId: $stateParams.projectId
                        };
                    }
                }
            });
            modalInstance.result.then(function (experiment) {
                $state.go('newexperiment', experiment);
            }, function () {
            });
        }
    });
