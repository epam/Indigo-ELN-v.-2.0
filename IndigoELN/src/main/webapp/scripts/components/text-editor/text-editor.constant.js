(function() {
    angular
        .module('indigoeln')
        .constant('textEditorConfig', {
            placeholder: 'Add a description',
            toolbar: ['title', 'bold', 'italic', 'underline', 'strikethrough', 'fontScale', 'color',
                'ol', 'ul', 'blockquote', 'table', 'link', 'image', 'hr', 'indent', 'outdent', 'alignment'],
            pasteImage: true,
            defaultImage: 'assets/images/image.gif'
        });
})();
