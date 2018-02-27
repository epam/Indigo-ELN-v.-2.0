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

var template = require('./reaction-scheme.html');

function indigoReactionScheme() {
    return {
        restrict: 'E',
        template: template,
        controller: IndigoReactionSchemeController,
        controllerAs: 'vm',
        bindToController: true,
        scope: {
            componentData: '=',
            reaction: '=',
            reactionTrigger: '=',
            experiment: '=',
            isReadonly: '=',
            onChanged: '&'
        }
    };
}

/* @ngInject */
function IndigoReactionSchemeController($scope, calculationService, $q, notifyService) {
    var vm = this;

    init();

    function init() {
        vm.onChangedStructure = onChangedStructure;
        vm.modelTrigger = 0;

        checkReaction();
        bindEvents();
    }

    function checkReaction() {
        if (_.get(vm.componentData, 'molfile') && !_.has(vm.componentData, 'molReactants')) {
            vm.modelTrigger++;
            notifyService.info('The old version of reaction scheme is updated. ' +
                'Save Experiment for apply changes');
        }
    }

    function bindEvents() {
        $scope.$on('new-reaction-scheme', function(event, reactionData) {
            if (reactionData.structure !== _.get(vm.componentData, 'molfile')) {
                vm.componentData.molfile = reactionData.structure;
                vm.componentData.image = null;
                vm.modelTrigger++;
            }
        });
    }

    function getInfo(molfiles) {
        return $q.all(_.map(molfiles, calculationService.getMoleculeInfo));
    }

    function updateReaction(response) {
        vm.componentData.molfile = response.structure;
        vm.componentData.molReactants = response.reactants;
        vm.componentData.molProducts = response.products;

        return $q.all([
            getInfo(vm.componentData.molReactants),
            getInfo(vm.componentData.molProducts)
        ]).then(function(results) {
            vm.componentData.infoReactants = moleculeInfoResponseCallback(results[0]);
            vm.componentData.infoProducts = moleculeInfoResponseCallback(results[1], true);
            vm.onChanged();
        });
    }

    function updateImage(structure) {
        if (structure.image !== vm.componentData.image) {
            vm.componentData.image = structure.image;
        }
    }

    function clear() {
        vm.componentData.molfile = null;
        vm.componentData.structureId = null;
        vm.componentData.image = null;
        vm.componentData.molReactants.length = 0;
        vm.componentData.molProducts.length = 0;
        vm.componentData.infoReactants.length = 0;
        vm.componentData.infoProducts.length = 0;
    }

    function onChangedStructure(structure) {
        if (!structure.molfile) {
            clear();
        }
        updateImage(structure);

        if (structure.molfile !== vm.componentData.molfile) {
            return calculationService.getReactionProductsAndReactants(structure.molfile)
                .then(function(response) {
                    return updateReaction(response, structure);
                });
        }

        return $q.reject();
    }

    function createReactant(isProduct, index, result) {
        return {
            chemicalName: isProduct ? getDefaultChemicalName(index) : result.name,
            formula: result.molecularFormula,
            molWeight: {
                value: result.molecularWeight
            },
            exactMass: result.exactMolecularWeight,
            structure: {
                image: result.image,
                molfile: result.molecule,
                structureType: 'molecule'
            }
        };
    }

    function moleculeInfoResponseCallback(results, isProduct) {
        if (!results || results.length === 0) {
            return null;
        }

        var moleculeInfo = [];

        _.forEach(results, function(result, index) {
            if (!_.isEmpty(result.molecularFormula)) {
                moleculeInfo.push(createReactant(isProduct, index, result));
            }
        });

        return moleculeInfo;
    }

    function getDefaultChemicalName(index) {
        return 'P' + index;
    }
}

module.exports = indigoReactionScheme;
