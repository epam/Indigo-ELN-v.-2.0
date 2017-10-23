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
            templateUrl: 'scripts/app/common/directives/additional-editor-value/additional-editor-value.html',
            controller: additionalEditorValueController,
            controllerAs: 'vm',
            bindToController: true
        };

        function additionalEditorValueController() {

        }
    }
})();
