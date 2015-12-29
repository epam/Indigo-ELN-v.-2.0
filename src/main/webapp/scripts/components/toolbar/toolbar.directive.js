(function () {
    'use strict';

    angular.module('indigoeln').directive('toolbar', toolbar);

    toolbar.$inject = ['$uibModal', '$rootScope'];

    function toolbar($uibModal, $rootScope) {
        return {
            scope: {},
            restrict: 'E',
            templateUrl: 'scripts/components/toolbar/toolbar.html',
            link: function (scope, elem, attrs) {
                scope.newExperiment = newExperiment;
                scope.newNotebook = newNotebook;
                scope.newProject = newProject;

                function newExperiment(event) {
                    $rootScope.$broadcast('new-experiment', {});
                    event.preventDefault();
                }

                function newNotebook(event) {
                    var modalInstance = $uibModal.open({
                        animation: true,
                        templateUrl: 'scripts/app/entities/notebook/new-notebook.html',
                        controller: 'NewNotebookCtrl'
                    });

                    modalInstance.result.then(function (notebook) {
                        $rootScope.$broadcast('created-notebook', {notebook: notebook});
                    }, function () {
                    });
                }

                function newProject(event) {
                    var modalInstance = $uibModal.open({
                      animation: true,
                      templateUrl: 'scripts/app/entities/project/new-project.html',
                      controller: 'NewProjectCtrl'
                    });

                    modalInstance.result.then(function (project) {
                        $rootScope.$broadcast('created-project', {project : project});
                    }, function () {
                    });
                }
            }
        }
    }
})();