/* @ngInject */
function iTranslate($filter) {
    return {
        compile: function(element, $attr) {
            var translatedStr = $filter('translate')($attr.iTranslate);

            element.html(translatedStr + ($attr.iEnd || ''));
        }
    };
}

module.exports = iTranslate;
