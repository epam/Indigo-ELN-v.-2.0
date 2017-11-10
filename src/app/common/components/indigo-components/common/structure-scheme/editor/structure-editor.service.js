editorUtils.$inject = ['$document'];

function editorUtils($document) {
    return {
        getEditor: function(frame) {
            var editor = null;
            if ('contentDocument' in frame) {
                editor = frame.contentWindow.ketcher;
            } else {
                // IE7
                editor = $document[0].frames.ifKetcher.window.ketcher;
            }

            return editor;
        },
        getMolfile: function(editor) {
            return editor.getMolfile();
        }
    };
}

module.exports = editorUtils;

