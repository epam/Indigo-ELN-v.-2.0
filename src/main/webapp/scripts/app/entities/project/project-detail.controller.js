'use strict';

angular.module('indigoeln')
    .controller('ProjectDetailController', function($scope, $rootScope, $uibModal, $state, Project, AlertService, Principal, project, users) {

        $scope.project = project;
        $scope.users = users;

        $scope.editDisabled = !Principal.hasAuthority('CONTENT_EDITOR');
        $scope.show = function(form) {
            if (!$scope.editDisabled) {
                form.$show();
            }
        };

        // 'PROJECT_READER'
        // 'PROJECT_CREATOR'
        // 'PROJECT_REMOVER'
        // 'CONTENT_EDITOR'

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
            AlertService.success('Project successfully saved');
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
            AlertService.error('Error saving project: ' + result);
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
                $state.go('newnotebook', {notebookName: notebookName, projectId: $scope.project.id});
            }, function () {
            });
        };


    });