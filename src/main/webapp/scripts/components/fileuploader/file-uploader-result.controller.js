'use strict';

angular.module('indigoeln')
    .controller('FileUploaderResultController', function ($scope, FileUploaderService, ParseLinks, Alert, $uibModal, $filter) {
        $scope.files = [];
        $scope.authorities = ['PROJECT_CREATOR', 'PROJECT_READER', 'CONTENT_EDITOR']; // TODO separate for READ actions and CREATE actions
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

        var thatScope = $scope;

        $scope.attach = function () {
            var modal = $uibModal.open({
                animation: true,
                size: 'lg',
                templateUrl: 'scripts/components/fileuploader/file-uploader-modal.html',
                controller: function ($scope, $uibModalInstance) {
                    $scope.entityid = entityid;
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                        thatScope.loadAll();
                    };
                }
            });
        };

        $scope.delete = function (id) {
            $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/fileuploader/file-delete-dialog.html',
                controller: function ($scope, $uibModalInstance) {
                    $scope.delete = function () {
                        FileUploaderService.delete({id: id})
                            .$promise.then(
                            function (result) {
                                Alert.success('File was successfully deleted');
                                thatScope.loadAll();
                                $uibModalInstance.close(true);
                            },
                            function (error) {
                                Alert.error('Error deleting the file: ' + error);
                                $uibModalInstance.close(true);
                            }
                        );
                    };
                    $scope.clear = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                }
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
