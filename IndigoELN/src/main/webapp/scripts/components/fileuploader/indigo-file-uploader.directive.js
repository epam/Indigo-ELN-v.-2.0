(function () {
    angular
        .module('indigoeln')
        .directive('indigoFileUploader', indigoFileUploader);

    function indigoFileUploader() {
        return {
            restrict: 'E',
            replace: true,
            controller: 'FileUploaderController',
            templateUrl: 'scripts/components/fileuploader/file-uploader.html',
            scope: {
                uploadUrl: '@',
                indigoReadonly: '='
            }
        };
    }
})();