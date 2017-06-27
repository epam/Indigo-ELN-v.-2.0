angular
    .module('indigoeln')
    .factory('AutoSaveEntitiesEngine', autoSaveEntitiesEngine);

/* @ngInject */
function autoSaveEntitiesEngine($q) {
    return {
        autoRecover: autoRecover
    };

    function autoRecover(service, params) {
        var deferred = $q.defer();
        var loadOrigin = function() {
            service.get(params, function(entity) {
                deferred.resolve(entity);
            });
        };
        loadOrigin();

        return deferred.promise;
    }
}

