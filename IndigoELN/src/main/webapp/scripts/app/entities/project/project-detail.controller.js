'use strict';

angular.module('indigoeln')
    .controller('ProjectDetailController', function($scope, $rootScope, $state, $uibModal, Project, User, data, $cookies) {

        $scope.project = data;
        $scope.users = User.query();

        // editor options
        var toolbar = ['title','bold','italic','underline','strikethrough','fontScale','color',
            'ol','ul','blockquote','table','link','image','hr','indent','outdent','alignment'];
        Simditor.locale = 'en_EN';
        var editor = new Simditor({
            textarea: $('#editor'),
            toolbar: toolbar,
            placeholder: '',
            pasteImage: true,
            defaultImage: 'assets/images/image.gif'
            //fileKey: 'upload_file',
            //upload: {
            //    url: '/api/project_files/123abc',
            //    params: {'X-CSRF-TOKEN': $cookies.get('CSRF-TOKEN')}
            //}
        });

        if ($scope.project.description) {
            editor.setValue($scope.project.description);
        }

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

            $scope.project.description = editor.getValue();
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

        var entityid = $scope.project.id;

        $scope.attachFile = function() {
            var modal = $uibModal.open({
                animation: true,
                size: 'lg',
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