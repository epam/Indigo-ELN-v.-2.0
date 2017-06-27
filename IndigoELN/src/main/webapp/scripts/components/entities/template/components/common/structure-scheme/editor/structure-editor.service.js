/* jshint browser: true */
(function() {
    angular
        .module('indigoeln')
        .factory('editorUtils', function() {
            return {
                getEditor: function(frame) {
                    // TODO update if new editor added
                    // find ketcher and define as editor
                    var editor = null;
                    if ('contentDocument' in frame) {
                        editor = frame.contentWindow.ketcher;
                    } else {
                        // IE7
                        editor = document.frames.ifKetcher.window.ketcher;
                    }

                    return editor;
                },
                getMolfile: function(editor) {
                    // TODO update if new editor added
                    return editor.getMolfile();
                }
            };
        });
})();
