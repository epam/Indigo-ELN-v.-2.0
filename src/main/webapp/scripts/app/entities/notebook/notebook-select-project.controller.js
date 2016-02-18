'use strict';

angular.module('indigoeln')
    .controller('NotebookSelectProjectController', function ($scope, $uibModalInstance, projects) {
        $scope.projects = projects;
        $scope.selectedProject = '';

        $scope.ok = okPressed;
        $scope.cancel = cancelPressed;

        function okPressed () {
            $uibModalInstance.close($scope.selectedProject.node.id);
        }

        function cancelPressed () {
            $uibModalInstance.dismiss();
        }
    });
