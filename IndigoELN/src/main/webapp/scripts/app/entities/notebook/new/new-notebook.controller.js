'use strict';

angular.module('indigoeln')
    .controller('NewNotebookController', function ($scope, $state, $stateParams, $uibModal, notebook, projectId) {
        $scope.notebook = notebook;
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
                            projectId: projectId
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
