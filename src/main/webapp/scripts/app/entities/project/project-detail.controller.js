'use strict';

angular.module('indigoeln')
    .controller('ProjectDetailController', function($scope, $rootScope, $state, $uibModal, Project, User, data) {

        $scope.project = data;
        $scope.users = User.query();

        var onSaveSuccess = function (result) {
            $scope.isSaving = false;
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
        };

        // prepare tags and keywords for UI
        $scope.tags = [];
        angular.forEach($scope.project.tags, function(tag) {
            $scope.tags.push({ text: tag});
        });
        $scope.keywords = '';
        if ($scope.project.keywords) {
            $scope.keywords = $scope.project.keywords.join(', ');
        }

        $scope.save = function () {
            $scope.isSaving = true;
            // prepare tags and keywords for Server
            $scope.project.tags = [];
            angular.forEach($scope.tags, function(tag, key) {
                $scope.project.tags.push(tag.text);
            });
            $scope.project.keywords = [];
            angular.forEach($scope.keywords.split(','), function(ref) {
                $scope.project.keywords.push(ref.trim());
            });
            Project.update($scope.project, onSaveSuccess, onSaveError);
        };

        $scope.newNotebook = function(event) {
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/notebook/new-notebook-dialog.html',
                controller: 'NewNotebookDialogController'
            });
            modalInstance.result.then(function (notebook) {
                $rootScope.$broadcast('created-notebook', {notebook: notebook});
            }, function () {
            });
        };

        var entityid = $scope.project.id;

        $scope.attachFile = function() {
            var modal = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/fileuploader/file-uploader-modal.html',
                controller: function ($scope, $uibModalInstance) {
                    $scope.entityid = entityid;
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                }
            });
        };

    });