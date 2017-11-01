(function() {
    angular
        .module('indigoeln')
        .directive('additionalEditorValue', additionalEditorValue);

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
            templateUrl: 'scripts/app/common/directives/additional-editor/additional-editor.html',
            controller: angular.noop,
            controllerAs: 'vm',
            bindToController: true
        };
    }
})();
