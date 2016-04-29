'use strict';

angular.module('indigoeln')
    .controller('FileUploaderController', function ($scope, FileUploaderService, FileUploaderCash, FileUploader,
                                                    $cookies, ParseLinks, Alert, $uibModal, $filter) {

        var entityid = $scope.entityid;
        $scope.page = 1;
        $scope.loadAll = function () {
            FileUploaderService.query({
                projectId: entityid,
                page: $scope.page - 1,
                size: 20
            }, function (result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.totalItems = headers('X-Total-Count');
                $scope.files = result;
            });
        };

        $scope.loadPage = function (page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.upload = function () {
            $uibModal.open({
                animation: true,
                size: 'lg',
                templateUrl: 'scripts/components/fileuploader/file-uploader-modal.html',
                controller: function ($scope, $uibModalInstance) {
                    $scope.files = [];
                    var formData = [];
                    if (entityid) {
                        formData.push({projectId: entityid});
                    }
                    var uploader = $scope.uploader = new FileUploader({
                        url: 'api/project_files',
                        alias: 'file',
                        headers: {
                            'X-CSRF-TOKEN': $cookies.get('CSRF-TOKEN')
                        },
                        formData: formData
                    });
                    // FILTERS // TODO: maximum permitted size 1048576 bytes
                    uploader.filters.push({
                        name: 'customFilter',
                        fn: function () {
                            return this.queue.length < 10;
                        }
                    });
                    // CALLBACKS
                    uploader.onWhenAddingFileFailed = function () {
                    };
                    uploader.onAfterAddingFile = function () {
                    };
                    uploader.onAfterAddingAll = function () {
                    };
                    uploader.onBeforeUploadItem = function () {
                    };
                    uploader.onProgressItem = function () {
                    };
                    uploader.onProgressAll = function () {
                    };
                    uploader.onErrorItem = function () {
                    };
                    uploader.onCancelItem = function () {
                    };
                    uploader.onCompleteItem = function () {
                    };
                    uploader.onCompleteAll = function() {};
                    uploader.onSuccessItem = function (fileItem, response) {
                        $scope.files.push(response);
                    };
                    $scope.remove = function (index) {
                        $scope.files.splice(index, 1);
                    };
                    $scope.cancel = function () {
                        FileUploaderCash.addFiles($scope.files);
                        $uibModalInstance.close($scope.files);
                    };
                }
            }).result.then(function (result) {
                $scope.files = _.union($scope.files, result);
            });
        };

        $scope.delete = function (file) {
            $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/fileuploader/file-delete-dialog.html',
                controller: function ($scope, $uibModalInstance) {
                    $scope.delete = function () {
                        if (entityid) {
                            FileUploaderService.delete({id: file.id})
                                .$promise.then(
                                function () {
                                    $uibModalInstance.close(file);
                                },
                                function (error) {
                                    Alert.error('Error deleting the file: ' + error);
                                    $uibModalInstance.close();
                                }
                            );
                        } else {
                            $uibModalInstance.close(file);
                        }
                    };
                    $scope.clear = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                }
            }).result.then(function (file) {
                $scope.files = _.without($scope.files, file);
                FileUploaderCash.removeFile(file);
                Alert.success('File was successfully deleted');
            });
        };
        $scope.search = function () {
            FileUploaderService.query({
                projectId: entityid,
                page: $scope.page - 1,
                size: 5
            }, function (result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.totalItems = headers('X-Total-Count');
                $scope.files = $filter('filter')(result, $scope.searchText);
            });
        };

    });