angular.module('indigoeln')
    .factory('FileUploaderCash', fileUploaderCash);

/* @ngInject */
function fileUploaderCash() {
    var _files;
    return {
        getFiles: getFiles,
        setFiles: setFiles,
        addFile: addFile,
        removeFile: removeFile,
        addFiles: addFiles
    };

    function getFiles() {
        return _files;
    }

    function setFiles(files) {
        _files = files;
    }

    function addFile(file) {
        if (_files) {
            _files.push(file);
        }
    }

    function removeFile(file) {
        if (_files) {
            _files = _.without(_files, file);
        }
    }

    function addFiles(files) {
        if (_files) {
            _files = _.union(_files, files);
        }
    }
}

