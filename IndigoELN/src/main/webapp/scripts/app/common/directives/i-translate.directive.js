(function() {
    angular
        .module('indigoeln')
        .directive('iTranslate', iTranslate);

    iTranslate.$inject = ['i18en'];

    function iTranslate(i18en) {
        return {
            compile: function(element, $attr) {
                element.html(i18en[$attr.iTranslate] + ($attr.iEnd || ''));
            }
        };
    }
})();
