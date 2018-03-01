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

run.$inject = ['$templateCache', 'typeOfComponents'];

function run($templateCache, typeOfComponents) {
    var defaultAttributes = ' model="vm.model"' +
        ' reactants="vm.reactants"' +
        ' reactants-trigger="vm.reactantsTrigger"' +
        ' experiment="vm.experiment"' +
        ' is-readonly="vm.isReadonly"' +
        ' on-changed="vm.onChangedComponent({componentId: component.id})"';

    var batchAttributes = defaultAttributes +
        ' batches="vm.batches"' +
        ' on-added-batch="vm.onAddedBatch(batch)"' +
        ' batches-trigger="vm.batchesTrigger"' +
        ' selected-batch="vm.selectedBatch"' +
        ' is-exist-stoich-table="::!!vm.model.stoichTable"' +
        ' selected-batch-trigger="vm.selectedBatchTrigger"' +
        ' on-select-batch="vm.onSelectBatch(batch)"' +
        ' on-remove-batches="vm.onRemoveBatches(batches)"' +
        ' batch-operation="vm.batchOperation"' +
        ' save-experiment-fn="vm.saveExperimentFn()"';

    var stoichTableAttributes = defaultAttributes +
        ' on-precursors-changed="vm.onPrecursorsChanged(precursors)"' +
        ' on-stoich-table-changed="vm.onStoichTableChanged(stoichTable)"' +
        ' info-reactants="vm.model.reaction.infoReactants"' +
        ' info-products="vm.model.reaction.infoProducts"';

    _.forEach(typeOfComponents, function(component) {
        $templateCache.put(component.id, getTemplate(component));
    });

    function getTemplate(component) {
        var directiveName = 'indigo-' + component.id;

        return '<' + directiveName + getComponentAttributes(component.id) +
            'component-data="vm.model.' + component.field + '"></' + directiveName + '>';
    }

    function getComponentAttributes(id) {
        var component = _.find(typeOfComponents, {id: id});
        if (component.isBatch) {
            return batchAttributes;
        }
        if (id === 'stoich-table') {
            return stoichTableAttributes;
        }

        return defaultAttributes;
    }
}

module.exports = run;
