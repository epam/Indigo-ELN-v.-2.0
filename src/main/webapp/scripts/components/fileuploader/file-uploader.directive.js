'use strict';

angular.module('indigoeln')
    .directive('elnFileUploader', function() {
        return {
            restrict: 'E',
            replace: true,
            controller: 'FileUploaderController',
            templateUrl: 'scripts/components/fileuploader/file-uploader.html',
            scope: {
                entity: '@',
                entityid: '='
            }
        };
    });