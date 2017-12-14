/* @ngInject */
function iTranslate(translateService) {
    return {
        compile: function(element, $attr) {
            var translatedStr = translateService.translate($attr.iTranslate);

            element.html(translatedStr + ($attr.iEnd || ''));
        }
    };
}

module.exports = iTranslate;
