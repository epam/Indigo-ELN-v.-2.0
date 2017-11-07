iTranslate.$inject = ['i18en', '$log'];

function iTranslate(i18en, $log) {
    return {
        compile: function(element, $attr) {
            if (!i18en[$attr.iTranslate]) {
                $log.error('Translate didn\'t find', $attr.iTranslate, element[0]);
            }
            element.html(i18en[$attr.iTranslate] + ($attr.iEnd || ''));
        }
    };
}

module.exports = iTranslate;
