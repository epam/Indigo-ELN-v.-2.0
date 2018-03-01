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
function stoichProductColumns(appUnits, appValuesService) {
    return {
        chemicalName: {
            id: 'chemicalName',
            name: 'Chemical Name',
            type: 'input',
            hasStructurePopover: true
        },
        formula: {
            id: 'formula',
            name: 'Formula',
            type: 'formula',
            readonly: true
        },
        molWeight: {
            id: 'molWeight',
            name: 'Mol.Wt.',
            type: 'scalar',
            readonly: true
        },
        exactMass: {
            id: 'exactMass',
            name: 'Exact Mass',
            type: 'primitive'
        },
        theoWeight: {
            id: 'theoWeight',
            name: 'Theo. Wgt.',
            type: 'unit',
            unitItems: appUnits.grams,
            readonly: true
        },
        theoMoles: {
            id: 'theoMoles',
            name: 'Theo. Moles',
            type: 'unit',
            unitItems: appUnits.moles,
            isIntended: true,
            readonly: true
        },
        saltCode: {
            id: 'saltCode',
            name: 'Salt Code',
            type: 'select',
            values: appValuesService.getSaltCodeValues(),
            showDefault: true
        },
        saltEq: {
            id: 'saltEq',
            name: 'Salt EQ',
            type: 'scalar',
            checkEnabled: function(o) {
                return o.saltCode && o.saltCode.value > 0;
            }
        },
        hazardComments: {
            id: 'hazardComments',
            name: 'Hazard Comments'
        },
        eq: {
            id: 'eq',
            name: 'EQ',
            type: 'scalar'
        }
    };
}

module.exports = stoichProductColumns;
