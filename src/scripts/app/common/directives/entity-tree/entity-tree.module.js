angular
    .module('indigoeln.entityTree', [])
    .run(function($rootScope, entityTreeService, principalService) {
        principalService.addUserChangeListener(entityTreeService.clearAll);
    });

