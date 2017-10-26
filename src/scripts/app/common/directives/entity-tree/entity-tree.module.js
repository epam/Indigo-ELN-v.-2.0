angular
    .module('indigoeln.entityTree', [])
    .run(function($rootScope, entityTreeService, Principal) {
        Principal.addUserChangeListener(entityTreeService.clearAll);
    });

