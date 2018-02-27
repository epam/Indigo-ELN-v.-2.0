/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
