angular
    .module('indigoeln.commonModule.servicesModule')
    .factory('appValues', appValuesFactory);

/* @ngInject */
function appValuesFactory(appUnits) {
    var grams = angular.copy(appUnits.grams);
    var liters = angular.copy(appUnits.liters);
    var moles = angular.copy(appUnits.moles);
    var density = angular.copy(appUnits.density);
    var molarity = angular.copy(appUnits.molarity);
    var rxnValues = angular.copy(appUnits.rxnValues);
    var rxnRoleReactant = angular.copy(appUnits.rxnRoleReactant);
    var rxnRoleSolvent = angular.copy(appUnits.rxnRoleSolvent);
    var compoundProtectionValues = angular.copy(appUnits.compoundProtectionValues);
    var loadFactorUnits = angular.copy(appUnits.loadFactorUnits);
    var defaultSaltCode = angular.copy(appUnits.defaultSaltCode);
    var saltCodeValues = angular.copy(appUnits.saltCodeValues);
    var defaultBatch = angular.copy(appUnits.defaultBatch);

    return {
        getGrams: getGrams,
        getLiters: getLiters,
        getMoles: getMoles,
        getDensity: getDensity,
        getMolarity: getMolarity,
        getRxnRoleReactant: getRxnRoleReactant,
        getRxnRoleSolvent: getRxnRoleSolvent,
        getRxnValues: getRxnValues,
        getDefaultSaltCode: getDefaultSaltCode,
        getSaltCodeValues: getSaltCodeValues,
        getCompoundProtectionValues: getCompoundProtectionValues,
        getLoadFactorUnits: getLoadFactorUnits,
        getDefaultBatch: getDefaultBatch
    };


    function getGrams() {
        return grams;
    }

    function getLiters() {
        return liters;
    }

    function getMoles() {
        return moles;
    }

    function getDensity() {
        return density;
    }

    function getMolarity() {
        return molarity;
    }

    function getRxnRoleReactant() {
        return rxnRoleReactant;
    }

    function getRxnRoleSolvent() {
        return rxnRoleSolvent;
    }

    function getRxnValues() {
        return rxnValues;
    }

    function getDefaultSaltCode() {
        return defaultSaltCode;
    }

    function getSaltCodeValues() {
        return saltCodeValues;
    }

    function getCompoundProtectionValues() {
        return compoundProtectionValues;
    }

    function getLoadFactorUnits() {
        return loadFactorUnits;
    }

    function getDefaultBatch() {
        return angular.copy(defaultBatch);
    }
}
