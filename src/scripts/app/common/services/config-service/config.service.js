/* @ngInject */
function configService() {
    var configuration = {};

    return {
        setConfiguration: setConfiguration,
        getConfiguration: getConfiguration
    };

    function setConfiguration(newConfig) {
        configuration = newConfig;
    }

    function getConfiguration() {
        return configuration;
    }
}

module.exports = configService;
