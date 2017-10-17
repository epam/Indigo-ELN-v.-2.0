(function() {
    angular
        .module('indigoeln')
        .directive('iTranslate', i18nDirective)
        .filter('iTranslate', i18nFilter);

    i18nDirective.$inject = ['i18en'];

    function i18nDirective(i18en) {
        return {
            compile: function(element, $attr) {
                element.html(($attr.iStart || '') + i18en[$attr.iTranslate] + ($attr.iEnd || ''));
            }
        };
    }

    i18nFilter.$inject = ['i18en'];

    function i18nFilter(i18en) {
        return function(iTranslate) {
            return i18en[iTranslate];
        };
    }
})();
