/* @ngInject */
function translate(i18en, $log) {
    return function(input) {
        if (!i18en[input]) {
            $log.error('Translate didn\'t find', input);

            return '';
        }

        return i18en[input];
    };
}

module.exports = translate;
