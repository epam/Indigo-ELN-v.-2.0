SingleFileUploaderController.$inject = ['$uibModalInstance', '$cookies', 'FileUploader', 'url'];

function SingleFileUploaderController($uibModalInstance, $cookies, FileUploader, url) {
    var vm = this;
    var uploader;

    vm.cancel = cancel;

    init();

    function init() {
        uploader = new FileUploader({
            url: url,
            alias: 'file',
            headers: {
                'X-CSRF-TOKEN': $cookies.get('CSRF-TOKEN')
            }
        });

        vm.uploader = uploader;

        uploader.filters.push({
            name: 'customFilter',
            fn: function() {
                return vm.queue.length < 1;
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

module.exports = SingleFileUploaderController;
