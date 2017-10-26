angular
    .module('indigoeln')
    .constant('searchReagentsConstant', {
        restrictions: {
            searchQuery: '',
            advancedSearch: {
                compoundId: {
                    name: 'Compound ID',
                    field: 'compoundId',
                    condition: {
                        name: 'contains'
                    }
                },
                fullNbkBatch: {
                    name: 'NBK batch #',
                    field: 'fullNbkBatch',
                    condition: {
                        name: 'contains'
                    }
                },
                formula: {
                    name: 'Molecular Formula',
                    field: 'formula',
                    condition: {
                        name: 'contains'
                    }
                },
                molWeight: {
                    name: 'Molecular Weight',
                    field: 'molWeight.value',
                    condition: {
                        name: '>'
                    }
                },
                chemicalName: {
                    name: 'Chemical Name',
                    field: 'chemicalName',
                    condition: {
                        name: 'contains'
                    }
                },
                externalNumber: {
                    name: 'External #',
                    field: 'externalNumber',
                    condition: {
                        name: 'contains'
                    }
                },
                compoundState: {
                    name: 'Compound State',
                    field: 'compoundState.name',
                    getValue: function(val) {
                        return val.name;
                    }
                },
                comments: {
                    name: 'Batch Comment',
                    field: 'comments',
                    condition: {
                        name: 'contains'
                    }
                },
                hazardComments: {
                    name: 'Batch Hazard Comment',
                    field: 'hazardComments',
                    condition: {
                        name: 'contains'
                    }
                },
                casNumber: {
                    name: 'CAS Number',
                    field: 'casNumber',
                    condition: {
                        name: 'contains'
                    }
                }
            },
            structure: {
                name: 'Reaction Scheme',
                similarityCriteria: {
                    name: 'equal'
                },
                similarityValue: null,
                image: null
            }
        }
    });
