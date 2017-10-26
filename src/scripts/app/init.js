(function() {
    var initInjector = angular.injector(['ng']);
    var $http = initInjector.get('$http');
    $http.get('api/client_configuration').then(
        function(response) {
            angular.module('config', []).constant('CONFIG', response.data);

            angular.element(document).ready(function() {
                angular.bootstrap(document, ['indigoeln']);
            });
        }
    );
})();
