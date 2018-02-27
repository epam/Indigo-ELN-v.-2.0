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

var BatchViewField = require('./batch-view-field');
var BaseBatchRow = require('../base-batch-row');
var fieldTypes = require('../../../../../services/calculation/field-types');
var calculationHelper = require('../../../../../services/calculation/calculation-helper.service');

function BatchViewRow(props) {
    var rowProps = getDefaultBatchViewRow();

    if (props && _.isObject(props)) {
        // Assign known properties from given obj
        // This will mutate rowProps object
        setRowProperties(rowProps, props);
    }

    _.defaults(this, rowProps);

    return this;
}

BatchViewRow.prototype = Object.create(BaseBatchRow.prototype);
BatchViewRow.constructor = BatchViewRow;

function setRowProperties(defaultProps, customProps) {
    // Assign known custom properties to default object
    _.forEach(customProps, function(value, key) {
        if (fieldTypes.isId(key)) {
            defaultProps[key] = value;
        } else if (fieldTypes.isMolWeight(key)) {
            defaultProps[key].value = value.value;
            defaultProps[key].baseValue = value.value;
            defaultProps[key].entered = value.entered;
        } else if (fieldTypes.isFormula(key)) {
            defaultProps[key].value = _.isObject(value) ? value.value : value;
            defaultProps[key].baseValue = _.isObject(value) ? value.baseValue : value;
        } else {
            defaultProps[key] = value;
        }
    });
}

function getDefaultBatchViewRow() {
    return {
        id: calculationHelper.getId(),
        nbkBatch: null,
        fullNbkBatch: null,
        fullNbkImmutablePart: null,
        virtualCompoundId: null,
        conversationalBatchNumber: null,
        registrationDate: null,
        registrationStatus: null,
        molWeight: {value: 0, baseValue: 0, entered: false},
        formula: {value: null, baseValue: null},
        stereoisomer: null,
        structureComments: null,
        saltCode: {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0, readonly: false},
        saltEq: {value: 0},
        structure: null,
        precursors: null,
        theoWeight: {value: 0, unit: 'mg'},
        theoMoles: {value: 0, unit: 'mmol'},
        totalWeight: new BatchViewField(0, 'mg'),
        totalMoles: new BatchViewField(0, 'mmol'),
        totalVolume: new BatchViewField(0, 'mL'),
        yield: 0,
        compoundState: null,
        purity: null,
        meltingPoint: null,
        source: null,
        sourceDetail: null,
        externalSupplier: null,
        healthHazards: null,
        compoundProtection: null,
        residualSolvents: null,
        solubility: null,
        storageInstructions: null,
        handlingPrecautions: null,
        comments: null,
        batchType: null
    };
}

module.exports = BatchViewRow;
