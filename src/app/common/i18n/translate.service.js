/* @ngInject */
function translateService(i18en, $log, $interpolate) {
    return {
        translate: function(input, interpolateData) {
            if (!_.has(i18en, input)) {
                $log.error('Translate didn\'t find', input);

                return '';
            }

            return $interpolate(i18en[input])(interpolateData);
        }
    };
}

module.exports = translateService;
