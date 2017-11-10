var appRoles = require('./app-roles');
var appUnits = require('./app-units');
var modelRestrictions = require('./model-restrictions');
var userPermissions = require('./user-permissions');

module.exports = angular
    .module('indigoeln.common.constants', [])

    .constant('appRoles', appRoles)
    .constant('appUnits', appUnits)
    .constant('modelRestrictions', modelRestrictions)
    .constant('userPermissions', userPermissions)

    .name;
