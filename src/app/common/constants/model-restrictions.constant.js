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

var modelRestrictions = {
    searchQuery: '',
    advancedSearch: {
        therapeuticArea: {
            name: 'Therapeutic Area',
            field: 'therapeuticArea',
            isSelect: true,
            condition: {name: 'contains'},
            $$conditionList: []
        },
        projectCode: {
            name: 'Project Code',
            field: 'projectCode',
            isSelect: true,
            condition: {name: 'contains'},
            $$conditionList: []
        },
        batchYield: {
            name: 'Batch Yield%',
            field: 'batchYield',
            type: 'number',
            condition: {name: '>'},
            $$conditionList: [
                {name: '>'}, {name: '<'}, {name: '='}, {name: '~'}
            ]
        },
        batchPurity: {
            name: 'Batch Purity%',
            field: 'purity',
            type: 'number',
            condition: {name: '>'},
            $$conditionList: [
                {name: '>'}, {name: '<'}, {name: '='}, {name: '~'}
            ]
        },
        subject: {
            name: 'Subject/Title',
            field: 'name',
            type: 'text',
            condition: {name: 'contains'},
            $$conditionList: [
                {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
            ]
        },
        entityDescription: {
            name: 'Entity Description',
            field: 'description',
            type: 'text',
            condition: {name: 'contains'},
            $$conditionList: [
                {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
            ]
        },
        compoundId: {
            name: 'Compound ID',
            field: 'compoundId',
            type: 'text',
            condition: {name: 'contains'},
            $$conditionList: [
                {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
            ]
        },
        literatureRef: {
            name: 'Literature Ref',
            field: 'references',
            type: 'text',
            condition: {name: 'contains'},
            $$conditionList: [
                {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
            ]
        },
        entityKeywords: {
            name: 'Entity Keywords',
            field: 'keywords',
            type: 'text',
            condition: {name: 'contains'},
            $$conditionList: [
                {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
            ]
        },
        chemicalName: {
            name: 'Chemical Name',
            field: 'chemicalName',
            type: 'text',
            condition: {name: 'contains'},
            $$conditionList: [
                {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
            ]
        },
        entityTypeCriteria: {
            name: 'Entity Type Criteria',
            $$skipList: true,
            field: 'kind',
            condition: {
                name: 'in'
            },
            value: []
        },

        entityDomain: {
            name: 'Entity Searching Domain',
            $$skipList: true,
            ownEntitySelected: false,
            field: 'author.$id',
            condition: {
                name: 'in'
            },
            value: []
        },

        statusCriteria: {
            name: 'Status',
            $$skipList: true,
            field: 'status',
            condition: {
                name: 'in'
            },
            value: []
        }
    },
    users: [],
    entityType: 'Project',
    structure: {
        name: 'Reaction Scheme',
        similarityCriteria: {
            name: 'equal'
        },
        similarityValue: null,
        image: null,
        type: {
            name: 'Product'
        }
    }
};

module.exports = modelRestrictions;
