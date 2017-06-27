(function() {
    angular
        .module('indigoeln')
        .directive('indigoTagInput', indigoTagInput);

    /* @ngInject */
    function indigoTagInput(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoLabel: '@',
                indigoLabelVertical: '=',
                indigoModel: '=',
                indigoReadonly: '=',
                indigoClasses: '@',
                indigoOnClick: '=',
                indigoOnAdding: '=',
                indigoPlaceholder: '@',
                indigoMaxTags: '=',
                indigoSource: '='
            },
            compile: compile,
            templateUrl: 'scripts/components/form/elements/tag-input/tag-input.html'
        };

        /* @ngInject */
        function compile(tElement, tAttrs) {
            formUtils.doVertical(tAttrs, tElement);
            var $tagInput = tElement.find('tags-input');
            if (tAttrs.indigoSource) {
                var autoComplete = '<auto-complete min-length="1" source="indigoSource($query)">';
                $tagInput.append(autoComplete);
            }
            formUtils.doVertical(tAttrs, tElement);
            formUtils.addDirectivesByAttrs(tAttrs, $tagInput);
            if (tAttrs.indigoOnClick) {
                $tagInput.attr('on-tag-clicked', 'indigoOnClick($tag)');
            }
            if (tAttrs.indigoOnAdding) {
                $tagInput.attr('on-tag-adding', 'indigoOnAdding($tag)');
            }
            if (tAttrs.indigoMaxTags) {
                $tagInput.attr('max-tags', '{{indigoMaxTags}}');
            }
            if (tAttrs.indigoMaxTags) {
                $tagInput.attr('placeholder', '{{indigoPlaceholder}}');
            }
        }
    }
})();
