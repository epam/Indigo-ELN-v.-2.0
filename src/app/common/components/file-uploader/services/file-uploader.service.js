function fileUploader() {
    var files;

    return {
        getFiles: getFiles,
        setFiles: setFiles,
        addFile: addFile,
        removeFile: removeFile,
        addFiles: addFiles
    };

    function getFiles() {
        return files;
    }

    function setFiles(newFiles) {
        files = newFiles;
    }

    function addFile(newFile) {
        if (files) {
            files.push(newFile);
        }
    }

    function removeFile(file) {
        if (files) {
            files = _.without(files, file);
        }
    }

    function addFiles(newFiles) {
        if (files) {
            files = _.union(files, newFiles);
        }
    }
}

module.exports = fileUploader;
