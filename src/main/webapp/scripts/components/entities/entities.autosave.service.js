angular
    .module('indigoeln')
    .factory('AutoSaveEntitiesEngine', AutoSaveEntitiesEngine);

/* @ngInject */
function AutoSaveEntitiesEngine($q){

    return {
        trackEntityChanges:  trackEntityChanges,
        autoRecover : autoRecover
    };


    function trackEntityChanges (entity, form, $scope, kind) {
        //replaced with autorecovery

    }

    function autoRecover(service, params) {
        var deferred = $q.defer();
        var loadOrigin = function () {
            service.get(params, function (entity) {
                deferred.resolve(entity);
            });
        };
        loadOrigin();
        return deferred.promise;
    }

}

