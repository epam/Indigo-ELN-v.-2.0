/* jshint browser: true */
(function() {
    angular
        .module('indigoeln')
        .factory('editorUtils', function() {
            return {
                getEditor: function(frame) {
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
                    return editor.getMolfile();
                }
            };
        });
})();
