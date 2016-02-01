'use strict';

angular.module('indigoeln')
    .directive('myFileUploaderResult', function() {
        return {
            restrict: 'E',
            replace: true,
            controller: 'FileUploaderResultController',
            templateUrl: 'scripts/components/fileuploader/file-uploader-result.html',
            scope: {
                entityid: '='
            }
        };
    });