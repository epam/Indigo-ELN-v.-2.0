angular.module('indigoeln')
    .controller('FileUploaderController', function ($scope, $rootScope, FileUploaderCash, FileUploader,
                                                    $cookies, $stateParams, ParseLinks, Alert, $uibModal, $filter,
                                                    ProjectFileUploaderService, ExperimentFileUploaderService) {
        var params = $stateParams;
        var uploadUrl = $scope.uploadUrl;
        var UploaderService = params.experimentId ? ExperimentFileUploaderService : ProjectFileUploaderService;
        $scope.page = 1;
        $scope.loadAll = function () {
            UploaderService.query({
                projectId: params.projectId,
                experimentId: params.projectId + '-' + params.notebookId + '-' + params.experimentId,
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
        if (params.projectId) {
            $scope.loadAll();
        }
        $scope.upload = function () {
            $uibModal.open({
                animation: true,
                size: 'lg',
                templateUrl: 'scripts/components/fileuploader/file-uploader-modal.html',
                controller: function ($scope, $uibModalInstance) {
                    $scope.files = [];
                    var formData = [];
                    var paramsForUpload = {
                        projectId: params.projectId,
                        experimentId: params.projectId + '-' + params.notebookId + '-' + params.experimentId
                    };
                    formData.push(paramsForUpload);
                    var uploader = $scope.uploader = new FileUploader({
                        url: uploadUrl,
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
                $rootScope.$broadcast("refresh after attach", 0);
                Alert.success("Attachments are saved successfully.");
            });
        };
        $scope.delete = function (file) {
            $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/fileuploader/file-delete-dialog.html',
                controller: function ($scope, $uibModalInstance) {
                    $scope.delete = function () {
                        if (params.projectId) {
                            UploaderService.delete({id: file.id}).$promise.then(
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
            UploaderService.query({
                projectId: params.projectId,
                experimentId: params.projectId + '-' + params.notebookId + '-' + params.experimentId,
                page: $scope.page - 1,
                size: 5
            }, function (result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.totalItems = headers('X-Total-Count');
                $scope.files = $filter('filter')(result, $scope.searchText);
            });
        };
    });