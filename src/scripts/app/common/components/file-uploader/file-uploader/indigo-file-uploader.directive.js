(function() {
    angular
        .module('indigoeln.fileUploader')
        .directive('indigoFileUploader', indigoFileUploader);

    function indigoFileUploader() {
        return {
            restrict: 'E',
            replace: true,
            controller: 'FileUploaderController',
            controllerAs: 'vm',
            bindToController: true,
            templateUrl: 'scripts/app/common/components/file-uploader/file-uploader/file-uploader.html',
            scope: {
                uploadUrl: '@',
                indigoReadonly: '=',
                onChanged: '&'
            }
        };
    }
})();
