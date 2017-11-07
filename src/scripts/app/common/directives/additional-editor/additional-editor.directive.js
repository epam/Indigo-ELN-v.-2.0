var template = require('./additional-editor.html');

function additionalEditorValue() {
    return {
        restrict: 'E',
        transclude: true,
        scope: {
            model: '=',
            isDisabled: '=',
            onClick: '&'
        },
        compile: function($element) {
            $element.addClass('additional-editor');
        },
        template: template,
        controller: angular.noop,
        controllerAs: 'vm',
        bindToController: true
    };
}

module.exports = additionalEditorValue;
