'use strict';

angular.module('indigoeln')
    .controller('ProjectDetailController', function($scope, $rootScope, $uibModal, Project, project,users) {

        $scope.project = project;
        $scope.users = users;

        // prepare tags and keywords for UI
        $scope.tags = [];
        angular.forEach($scope.project.tags, function(tag) {
            $scope.tags.push({ text: tag});
        });
        $scope.keywords = '';
        if ($scope.project.keywords) {
            $scope.keywords = $scope.project.keywords.join(', ');
        }

        var onSaveSuccess = function (result) {
            $scope.isSaving = false;
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
        };

        $scope.save = function () {
            $scope.isSaving = true;
            // prepare tags and keywords for Server
            $scope.project.tags = [];
            if ($scope.tags) {
                angular.forEach($scope.tags, function(tag, key) {
                    $scope.project.tags.push(tag.text);
                });
            }
            $scope.project.keywords = [];
            if ($scope.keywords) {
                angular.forEach($scope.keywords.split(','), function(ref) {
                    $scope.project.keywords.push(ref.trim());
                });
            }
            Project.update($scope.project, onSaveSuccess, onSaveError);
        };

        $scope.newNotebook = function(event) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/notebook/new/dialog/new-notebook-dialog.html',
                controller: 'NewNotebookDialogController',
                size: 'lg'
            });
            modalInstance.result.then(function (notebookName) {
                $rootScope.$broadcast('created-notebook', {notebookName: notebookName});
                $state.go('notebook.new', {notebookName: notebookName, projectId: $scope.project.id});
            }, function () {
            });
        };


    });