/* @ngInject */
function translateService(i18en, $log) {
    return {
        translate: function(input) {
            if (!_.has(i18en, input)) {
                $log.error('Translate didn\'t find', input);

                return '';
            }

            return i18en[input];
        }
    };
}

module.exports = translateService;
