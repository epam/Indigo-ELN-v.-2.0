(function() {
    angular
        .module('indigoeln')
        .directive('indigoFileUploader', indigoFileUploader);

    function indigoFileUploader() {
        return {
            restrict: 'E',
            replace: true,
            controller: 'FileUploaderController',
            controllerAs: 'vm',
            bindToController: true,
            templateUrl: 'scripts/components/fileuploader/file-uploader/file-uploader.html',
            scope: {
                uploadUrl: '@',
                indigoReadonly: '=',
                onChanged: '&'
            }
        };
    }
})();
