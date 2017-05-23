angular.module('indigoeln')
    .controller('SingleFileUploaderController', function ($scope, $uibModalInstance, $cookies, $log, FileUploader, url) {
        var uploader = $scope.uploader = new FileUploader({
            url: url,
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
        uploader.onErrorItem = function () {
            $uibModalInstance.dismiss();
        };
        uploader.onSuccessItem = function (fileItem, response) {
            $uibModalInstance.close(response);
        };
        $scope.cancel = function () {
            if (uploader.queue.length === 1 && !uploader.queue[0].isUploading) {
                uploader.queue[0].cancel();
            }
            $uibModalInstance.close();
        };
    });