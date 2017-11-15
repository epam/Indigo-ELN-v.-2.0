var appRoles = require('./app-roles.constant');
var appUnits = require('./app-units.constant');
var modelRestrictions = require('./model-restrictions.constant');
var userPermissions = require('./user-permissions.constant');

module.exports = angular
    .module('indigoeln.common.constants', [])

    .constant('appRoles', appRoles)
    .constant('appUnits', appUnits)
    .constant('modelRestrictions', modelRestrictions)
    .constant('userPermissions', userPermissions)

    .name;
