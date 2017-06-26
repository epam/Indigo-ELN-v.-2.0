(function() {
    angular
        .module('indigoeln')
        .directive('indigoTextArea', indigoTextArea);

    /* @ngInject */
    function indigoTextArea(formUtils) {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            scope: {
                indigoLabel: '@',
                indigoModel: '=',
                indigoLabelVertical: '=',
                indigoClasses: '@',
                indigoNoElastic: '=',
                indigoInputGroup: '@',
                indigoReadonly: '=',
                indigoRowsNum: '=',
                indigoTooltip: '=',
                indigoTrim: '='
            },
            compile: compile,
            templateUrl: 'scripts/components/form/elements/text-area/text-area.html'
        };

        /* @ngInject */
        function compile(tElement, tAttrs) {
            if (tAttrs.indigoInputGroup) {
                var inputGroup = tElement.find('textarea').wrap('<div class="input-group"/>').parent();
                var element = '<div class="input-group-btn" style="vertical-align: top;" ng-transclude/>';
                if (tAttrs.indigoInputGroup === 'append') {
                    inputGroup.append(element);
                } else if (tAttrs.indigoInputGroup === 'prepend') {
                    inputGroup.prepend(element);
                }
            }
            if (tAttrs.indigoRowsNum) {
                tElement.find('textarea').attr('rows', tAttrs.indigoRowsNum);
            }
            formUtils.doVertical(tAttrs, tElement);
            formUtils.addDirectivesByAttrs(tAttrs, tElement.find('textarea'));

            if (tAttrs.indigoTrim) {
                tElement.find('textarea').attr('ng-trim', tAttrs.indigoTrim);
            }

            if (tAttrs.indigoNoElastic) {
                tElement.find('textarea').removeAttr('msd-elastic');
            }
        }
    }
})();
