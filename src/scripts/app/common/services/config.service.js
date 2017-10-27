(function() {
    angular.module('indigoeln')
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