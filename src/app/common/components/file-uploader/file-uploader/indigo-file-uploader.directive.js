var template = require('./file-uploader.html');

function indigoFileUploader() {
    return {
        restrict: 'E',
        replace: true,
        controller: 'FileUploaderController',
        controllerAs: 'vm',
        bindToController: true,
        template: template,
        scope: {
            uploadUrl: '@',
            indigoReadonly: '=',
            onChanged: '&'
        }
    };
}

module.exports = indigoFileUploader;
