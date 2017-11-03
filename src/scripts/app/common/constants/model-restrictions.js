angular
    .module('indigoeln.constantsModule')
    .constant('modelRestrictions', {
        searchQuery: '',
        advancedSearch: {
            therapeuticArea: {
                name: 'Therapeutic Area', field: 'therapeuticArea', isSelect: true, condition: {name: 'contains'},
                $$conditionList: [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]
            },
            projectCode: {
                name: 'Project Code',
                field: 'projectCode',
                isSelect: true,
                condition: {name: 'contains'},
                $$conditionList: [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]
            },
            batchYield: {
                name: 'Batch Yield%', field: 'batchYield', condition: {name: '>'}, $$conditionList: [
                    {name: '>'}, {name: '<'}, {name: '='}, {name: '~'}
                ]
            },
            batchPurity: {
                name: 'Batch Purity%', field: 'purity', condition: {name: '>'}, $$conditionList: [
                    {name: '>'}, {name: '<'}, {name: '='}, {name: '~'}
                ]
            },
            subject: {
                name: 'Subject/Title', field: 'name', condition: {name: 'contains'}, $$conditionList: [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]
            },
            entityDescription: {
                name: 'Entity Description', field: 'description', condition: {name: 'contains'}, $$conditionList: [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]
            },
            compoundId: {
                name: 'Compound ID', field: 'compoundId', condition: {name: 'contains'}, $$conditionList: [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]
            },
            literatureRef: {
                name: 'Literature Ref', field: 'references', condition: {name: 'contains'}, $$conditionList: [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]
            },
            entityKeywords: {
                name: 'Entity Keywords', field: 'keywords', condition: {name: 'contains'}, $$conditionList: [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]
            },
            chemicalName: {
                name: 'Chemical Name', field: 'chemicalName', condition: {name: 'contains'}, $$conditionList: [
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
                field: 'author._id',
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
    });
