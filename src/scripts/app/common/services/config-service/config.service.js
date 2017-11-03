(function() {
    angular
        .module('indigo.commonModule.servicesModule')
        .factory('configService', configService);

    configService.$inject = [];
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
})();