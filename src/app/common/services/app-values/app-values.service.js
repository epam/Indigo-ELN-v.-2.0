/* @ngInject */
function appValuesService($http, appUnits, apiUrl) {
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
    var defaultBatch = angular.copy(appUnits.defaultBatch);
    var saltCodeValues;

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
        getDefaultBatch: getDefaultBatch,
        fetchSaltCodes: fetchSaltCodes
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

    function fetchSaltCodes() {
        var config = {
            url: apiUrl + 'calculations/salt_code_table',
            method: 'GET',
            params: {tableName: 'GCM_SALT_CDT'},
            cache: true
        };

        return $http(config)
            .then(function(resp) {
                saltCodeValues = convertSaltCodes(resp.data);
            });
    }

    function convertSaltCodes(data) {
        return _.map(data, function(item) {
            var regValue = getRegValue(item.SALT_CODE);

            return {
                value: item.SALT_CODE,
                weight: +item.SALT_WEIGHT,
                name: regValue + ' - ' + item.SALT_DESC,
                regValue: regValue
            };
        });
    }

    function getRegValue(value) {
        return (value < 10) ? ('0' + value) : value;
    }
}

module.exports = appValuesService;
