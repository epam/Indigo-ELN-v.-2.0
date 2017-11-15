MultipleFileUploaderController.$inject = ['$uibModalInstance', '$cookies', 'notifyService', 'fileUploaderService',
    'FileUploader', 'params', 'uploadUrl'];

function MultipleFileUploaderController($uibModalInstance, $cookies, notifyService, fileUploaderService, FileUploader,
                                        params, uploadUrl) {
    var vm = this;
    var formData = [];
    var uploader;
    var paramsForUpload;

    vm.files = [];

    vm.remove = remove;
    vm.cancel = cancel;

    init();

    function init() {
        paramsForUpload = {
            projectId: params.projectId,
            experimentId: params.projectId + '-' + params.notebookId + '-' + params.experimentId
        };

        formData.push(paramsForUpload);

        uploader = new FileUploader({
            url: uploadUrl,
            alias: 'file',
            headers: {
                'X-CSRF-TOKEN': $cookies.get('CSRF-TOKEN')
            },
            formData: formData
        });

        vm.uploader = uploader;

        uploader.filters.push({
            name: 'customFilter',
            fn: function() {
                return this.queue.length < 10;
            }
        });
        // CALLBACKS
        uploader.onSuccessItem = function(fileItem, response) {
            vm.files.push(response);
            notifyService.success('Attachments are saved successfully.');
        };
        uploader.onErrorItem = function() {
            notifyService.error('Uploaded file size should be less than 10 Mb');
        };
    }

    function remove(index) {
        vm.files.splice(index, 1);
    }

    function cancel() {
        fileUploaderService.addFiles(vm.files);
        $uibModalInstance.close(vm.files);
    }
}

module.exports = MultipleFileUploaderController;
