angular.module('indigoeln')
    .controller('SingleFileUploaderController', function ($scope, $uibModalInstance, $cookies, $log, FileUploader) {
        var uploader = $scope.uploader = new FileUploader({
            url: 'api/project_files', // FIXME
            alias: 'file',
            headers: {
                'X-CSRF-TOKEN': $cookies.get('CSRF-TOKEN')
            }
        });
        // FILTERS // TODO: maximum permitted size 1048576 bytes
        uploader.filters.push({
            name: 'customFilter',
            fn: function () {
                return this.queue.length < 1;
            }
        });
        uploader.onAfterAddingFile = function (fileItem) {
            fileItem.upload();
        };
        uploader.onSuccessItem = function (fileItem, response) {
            $log.debug(fileItem, response);
        };
        $scope.cancel = function () {
            if (uploader.queue.length === 1 && !uploader.queue[0].isUploading) {
                uploader.queue[0].cancel();
            }
            $uibModalInstance.close();
        };
        $scope.ok = function () {
            $uibModalInstance.close();
        };
    });