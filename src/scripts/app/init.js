(function() {
    var initInjector = angular.injector(['ng']);
    var $http = initInjector.get('$http');
    var configInjector = angular.injector(['config']);

    $http.get(configInjector.get('apiUrl') + 'client_configuration').then(
        function(response) {
            angular.module('config')
                .constant('CONFIG', response.data);

            angular.element(document).ready(function() {
                angular.bootstrap(document, ['indigoeln']);
            });
        }
    );
})();
