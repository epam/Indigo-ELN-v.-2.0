'use strict';

angular.module('indigoeln')
    .directive('myFileUploader', function() {
        return {
            restrict: 'E',
            replace: true,
            controller: 'FileUploaderController',
            templateUrl: 'scripts/components/fileuploader/file-uploader.html',
            scope: {
                entityid: '=',
                myReadonly: '=',
                files: '='
            }
        };
    });