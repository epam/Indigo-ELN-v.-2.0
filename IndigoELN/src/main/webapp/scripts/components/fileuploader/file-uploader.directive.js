angular.module('indigoeln')
    .directive('myFileUploader', function() {
        return {
            restrict: 'E',
            replace: true,
            controller: 'FileUploaderController',
            templateUrl: 'scripts/components/fileuploader/file-uploader.html',
            scope: {
                uploadUrl: '@',
                myReadonly: '='
            }
        };
    });