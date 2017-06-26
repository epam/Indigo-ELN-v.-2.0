(function() {
    angular
        .module('indigoeln')
        .controller('SingleFileUploaderController', SingleFileUploaderController);

    /* @ngInject */
    function SingleFileUploaderController($uibModalInstance, $cookies, FileUploader, url) {
        var vm = this;
        var uploader;

        vm.cancel = cancel;

        init();

        function init() {
            uploader = vm.uploader = new FileUploader({
                url: url,
                alias: 'file',
                headers: {
                    'X-CSRF-TOKEN': $cookies.get('CSRF-TOKEN')
                }
            });

            // FILTERS
            // TODO: maximum permitted size 1048576 bytes
            uploader.filters.push({
                name: 'customFilter',
                fn: function() {
                    return this.queue.length < 1;
                }
            });

            uploader.onAfterAddingFile = function(fileItem) {
                fileItem.upload();
            };
            uploader.onErrorItem = function() {
                $uibModalInstance.dismiss();
            };
            uploader.onSuccessItem = function(fileItem, response) {
                $uibModalInstance.close(response);
            };
        }

        function cancel() {
            if (uploader.queue.length === 1 && !uploader.queue[0].isUploading) {
                uploader.queue[0].cancel();
            }
            $uibModalInstance.close();
        }
    }
})();