angular
    .module('indigoeln')
    .factory('sdProperties', sdProperties);

/* @ngInject */
function sdProperties(AppValues) {
    var constants = [
        {
            export: {
                name: 'REGISTRATION_STATUS',
                prop: ['registrationStatus']
            },
            import: {
                name: 'registrationStatus',
                code: 'REGISTRATION_STATUS',
                format: undefined
            }
        },
        {
            export: {
                name: 'CONVERSATIONAL_BATCH_NUMBER',
                prop: ['conversationalBatchNumber']
            },
            import: {
                name: 'conversationalBatchNumber',
                code: 'CONVERSATIONAL_BATCH_NUMBER',
                format: undefined
            }
        },
        {
            export: {
                name: 'VIRTUAL_COMPOUND_ID',
                prop: ['virtualCompoundId']
            },
            import: {
                name: 'virtualCompoundId',
                code: 'VIRTUAL_COMPOUND_ID',
                format: undefined
            }
        },
        {
            export: {
                name: 'COMPOUND_SOURCE_CODE',
                prop: ['source', 'name']
            },
            import: {
                name: 'source',
                code: 'COMPOUND_SOURCE_CODE',
                format: function(dicts, value) {
                    return getWord(dicts, 'Source', 'name', value);
                }
            }
        },
        {
            export: {
                name: 'COMPOUND_SOURCE_DETAIL_CODE',
                prop: ['sourceDetail', 'name']
            },
            import: {
                name: 'sourceDetail',
                code: 'COMPOUND_SOURCE_DETAIL_CODE',
                format: function(dicts, value) {
                    return getWord(dicts, 'Source Details', 'name', value);
                }
            }
        },
        {
            export: {
                name: 'STEREOISOMER_CODE',
                prop: ['stereoisomer', 'name']
            },
            import: {
                name: 'stereoisomer',
                code: 'STEREOISOMER_CODE',
                format: function(dicts, value) {
                    return getWord(dicts, 'Stereoisomer Code', 'name', value);
                }
            }
        },
        {
            export: {
                name: 'GLOBAL_SALT_CODE',
                prop: ['saltCode', 'regValue']
            },
            import: {
                name: 'saltCode',
                code: 'GLOBAL_SALT_CODE',
                format: function(dicts, value) {
                    return getItem(AppValues.getSaltCodeValues(), 'regValue', value);
                }
            }
        },
        {
            export: {
                name: 'GLOBAL_SALT_EQ',
                prop: ['saltEq', 'value']
            },
            import: {
                name: 'saltEq',
                code: 'GLOBAL_SALT_EQ',
                format: function(dicts, value) {
                    return {
                        value: parseInt(value),
                        entered: false
                    };
                }
            }
        },
        {
            export: {
                name: 'STRUCTURE_COMMENT',
                prop: ['structureComments']
            },
            import: {
                name: 'structureComments',
                code: 'STRUCTURE_COMMENT',
                format: undefined
            }
        },
        {
            export: {
                name: 'COMPOUND_STATE',
                prop: ['compoundState', 'name']
            },
            import: {
                name: 'compoundState',
                code: 'COMPOUND_STATE',
                format: function(dicts, value) {
                    return getWord(dicts, 'Compound State', 'name', value);
                }
            }
        },
        {
            export: {
                name: 'PRECURSORS',
                prop: ['precursors']
            },
            import: {
                name: 'precursors',
                code: 'PRECURSORS',
                format: undefined
            }
        },
        {
            export: {
                name: 'PURITY_STRING',
                prop: ['purity', 'asString']
            },
            import: {
                name: 'purity',
                code: 'PURITY_STRING',
                format: function(dicts, value) {
                    return {
                        asString: value
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_0_COMENTS',
                prop: ['purity', 'data', '0', 'comments']
            },
            import: {
                name: 'purity',
                code: 'PURITY_0_COMENTS',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                comments: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_0_DETERMINED',
                prop: ['purity', 'data', '0', 'determinedBy']
            },
            import: {
                name: 'purity',
                code: 'PURITY_0_DETERMINED',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                determinedBy: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_0_OPERATOR_NAME',
                prop: ['purity', 'data', '0', 'operator', 'name']
            },
            import: {
                name: 'purity',
                code: 'PURITY_0_OPERATOR_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                operator: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_0_VALUE',
                prop: ['purity', 'data', '0', 'value']
            },
            import: {
                name: 'purity',
                code: 'PURITY_0_VALUE',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                value: parseInt(value)
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_1_COMENTS',
                prop: ['purity', 'data', '1', 'comments'],
                subProp: undefined
            },
            import: {
                name: 'purity',

                code: 'PURITY_1_COMENTS',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                comments: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_1_DETERMINED',

                prop: ['purity', 'data', '1', 'determinedBy']
            },
            import: {
                name: 'purity',
                code: 'PURITY_1_DETERMINED',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                determinedBy: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_1_OPERATOR_NAME',
                prop: ['purity', 'data', '1', 'operator', 'name']
            },
            import: {
                name: 'purity',
                code: 'PURITY_1_OPERATOR_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                operator: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_1_VALUE',
                prop: ['purity', 'data', '1', 'value']
            },
            import: {
                name: 'purity',
                code: 'PURITY_1_VALUE',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                value: parseInt(value)
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_2_COMENTS',
                prop: ['purity', 'data', '2', 'comments']
            },
            import: {
                name: 'purity',
                code: 'PURITY_2_COMENTS',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                comments: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_2_DETERMINED',
                prop: ['purity', 'data', '2', 'determinedBy']
            },
            import: {
                name: 'purity',
                code: 'PURITY_2_DETERMINED',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                determinedBy: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_2_OPERATOR_NAME',
                prop: ['purity', 'data', '2', 'operator', 'name']
            },
            import: {
                name: 'purity',
                code: 'PURITY_2_OPERATOR_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                operator: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_2_VALUE',
                prop: ['purity', 'data', '2', 'value']
            },
            import: {
                name: 'purity',
                code: 'PURITY_2_VALUE',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                value: parseInt(value)
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_3_COMENTS',
                prop: ['purity', 'data', '3', 'comments']
            },
            import: {
                name: 'purity',
                code: 'PURITY_3_COMENTS',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                comments: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_3_DETERMINED',
                prop: ['purity', 'data', '3', 'determinedBy']
            },
            import: {
                name: 'purity',
                code: 'PURITY_3_DETERMINED',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                determinedBy: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_3_OPERATOR_NAME',
                prop: ['purity', 'data', '3', 'operator', 'name']
            },
            import: {
                name: 'purity',
                code: 'PURITY_3_OPERATOR_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                operator: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_3_VALUE',
                prop: ['purity', 'data', '3', 'value']
            },
            import: {
                name: 'purity',
                code: 'PURITY_3_VALUE',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                value: parseInt(value)
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_4_COMENTS',
                prop: ['purity', 'data', '4', 'comments']
            },
            import: {
                name: 'purity',
                code: 'PURITY_4_COMENTS',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                comments: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_4_DETERMINED',
                prop: ['purity', 'data', '4', 'determinedBy']
            },
            import: {
                name: 'purity',
                code: 'PURITY_4_DETERMINED',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                determinedBy: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_4_OPERATOR_NAME',
                prop: ['purity', 'data', '4', 'operator', 'name']
            },
            import: {
                name: 'purity',
                code: 'PURITY_4_OPERATOR_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                operator: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'PURITY_4_VALUE',
                prop: ['purity', 'data', '4', 'value']
            },
            import: {
                name: 'purity',
                code: 'PURITY_4_VALUE',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                value: parseInt(value)
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'MELTING_POINT',
                prop: ['meltingPoint', 'asString']
            },
            import: {
                name: 'meltingPoint',
                code: 'MELTING_POINT',
                format: function(dicts, value) {
                    return {
                        asString: value
                    };
                }
            }
        },
        {
            export: {
                name: 'MELTING_POINT_COMMENTS',
                prop: ['meltingPoint', 'comments']
            },
            import: {
                name: 'meltingPoint',
                code: 'MELTING_POINT_COMMENTS',
                format: function(dicts, value) {
                    return {
                        comments: value
                    };
                }
            }
        },
        {
            export: {
                name: 'MELTING_POINT_LOWER',
                prop: ['meltingPoint', 'lower']
            },
            import: {
                name: 'meltingPoint',
                code: 'MELTING_POINT_LOWER',
                format: function(dicts, value) {
                    return {
                        lower: value
                    };
                }
            }
        },
        {
            export: {
                name: 'MELTING_POINT_UPPER',
                prop: ['meltingPoint', 'upper']
            },
            import: {
                name: 'meltingPoint',
                code: 'MELTING_POINT_UPPER',
                format: function(dicts, value) {
                    return {
                        upper: value
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_STRING',
                prop: ['residualSolvents', 'asString']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_STRING',
                format: function(dicts, value) {
                    return {
                        asString: value
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_0_COMMENT',
                prop: ['residualSolvents', 'data', '0', 'comment']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_0_COMMENT',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                comment: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_0_EQ',
                prop: ['residualSolvents', 'data', '0', 'eq']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_0_EQ',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                eq: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_0_RANK',
                prop: ['residualSolvents', 'data', '0', 'name', 'rank']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_0_RANK',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                name: {
                                    rank: parseInt(value)
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_0_NAME',
                prop: ['residualSolvents', 'data', '0', 'name', 'name']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_0_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                name: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_1_COMMENT',
                prop: ['residualSolvents', 'data', '1', 'comment']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_1_COMMENT',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                comment: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_1_EQ',
                prop: ['residualSolvents', 'data', '1', 'eq']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_1_EQ',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                eq: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_1_RANK',
                prop: ['residualSolvents', 'data', '1', 'name', 'rank']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_1_RANK',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                name: {
                                    rank: parseInt(value)
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_1_NAME',
                prop: ['residualSolvents', 'data', '1', 'name', 'name']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_1_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                name: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_2_COMMENT',
                prop: ['residualSolvents', 'data', '2', 'comment']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_2_COMMENT',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                comment: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_2_EQ',
                prop: ['residualSolvents', 'data', '2', 'eq']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_2_EQ',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                eq: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_2_RANK',
                prop: ['residualSolvents', 'data', '2', 'name', 'rank']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_2_RANK',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                name: {
                                    rank: parseInt(value)
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_2_NAME',
                prop: ['residualSolvents', 'data', '2', 'name', 'name']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_2_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                name: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_3_COMMENT',
                prop: ['residualSolvents', 'data', '3', 'comment']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_3_COMMENT',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                comment: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_3_EQ',
                prop: ['residualSolvents', 'data', '3', 'eq']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_3_EQ',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                eq: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_3_RANK',
                prop: ['residualSolvents', 'data', '3', 'name', 'rank']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_3_RANK',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                name: {
                                    rank: parseInt(value)
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_3_NAME',
                prop: ['residualSolvents', 'data', '3', 'name', 'name']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_3_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                name: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_4_COMMENT',
                prop: ['residualSolvents', 'data', '4', 'comment']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_4_COMMENT',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                comment: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_4_EQ',
                prop: ['residualSolvents', 'data', '4', 'eq']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_4_EQ',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                eq: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_4_RANK',
                prop: ['residualSolvents', 'data', '4', 'name', 'rank']
            },
            import: {
                name: 'residualSolvents',
                code: 'RESIDUAL_SOLVENTS_4_RANK',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                name: {
                                    rank: parseInt(value)
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_4_NAME',
                prop: ['residual', 'data', '4', 'name', 'name']
            },
            import: {
                name: 'residual',
                code: 'RESIDUAL_SOLVENTS_4_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                name: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_STRING',
                prop: ['solubility', 'asString']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_STRING',
                format: function(dicts, value) {
                    return {
                        asString: value
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_0_COMMENT',
                prop: ['solubility', 'data', '0', 'comment']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_0_COMMENT',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                comment: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_0_TYPE',
                prop: ['solubility', 'data', '0', 'type', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_0_TYPE',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                type: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_0_RANK',
                prop: ['solubility', 'data', '0', 'solventName', 'rank']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_0_RANK',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                solventName: {
                                    rank: parseInt(value)
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_0_NAME',
                prop: ['solubility', 'data', '0', 'solventName', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_0_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                solventName: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_0_ENABLE',
                prop: ['solubility', 'data', '0', 'solventName', 'enable']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_0_ENABLE',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                solventName: {
                                    enable: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_0_VALUE_NAME',
                prop: ['solubility', 'data', '0', 'value', 'value', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_0_VALUE_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                value: {
                                    value: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_0_VALUE',
                prop: ['solubility', 'data', '0', 'value', 'value']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_0_VALUE',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                value: {
                                    value: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_0_VALUE_OPERATOR',
                prop: ['solubility', 'data', '0', 'value', 'operator', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_0_VALUE_OPERATOR',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                value: {
                                    operator: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_0_VALUE_UNIT',
                prop: ['solubility', 'data', '0', 'value', 'unit', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_0_VALUE_UNIT',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: {
                                value: {
                                    unit: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_1_COMMENT',
                prop: ['solubility', 'data', '1', 'comment']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_1_COMMENT',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                comment: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_1_TYPE',
                prop: ['solubility', 'data', '1', 'type', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_1_TYPE',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                type: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_1_RANK',
                prop: ['solubility', 'data', '1', 'solventName', 'rank']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_1_RANK',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                solventName: {
                                    rank: parseInt(value)
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_1_NAME',
                prop: ['solubility', 'data', '1', 'solventName', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_1_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                solventName: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_1_ENABLE',
                prop: ['solubility', 'data', '1', 'solventName', 'enable']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_1_ENABLE',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                solventName: {
                                    enable: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_1_VALUE_NAME',
                prop: ['solubility', 'data', '1', 'value', 'value', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_1_VALUE_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                value: {
                                    value: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_1_VALUE',
                prop: ['solubility', 'data', '1', 'value', 'value']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_1_VALUE',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                value: {
                                    value: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_1_VALUE_OPERATOR',
                prop: ['solubility', 'data', '1', 'value', 'operator', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_1_VALUE_OPERATOR',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                value: {
                                    operator: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_1_VALUE_UNIT',
                prop: ['solubility', 'data', '1', 'value', 'unit', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_1_VALUE_UNIT',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: {
                                value: {
                                    unit: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_2_COMMENT',
                prop: ['solubility', 'data', '2', 'comment']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_2_COMMENT',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                comment: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_2_TYPE',
                prop: ['solubility', 'data', '2', 'type', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_2_TYPE',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                type: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_2_RANK',
                prop: ['solubility', 'data', '2', 'solventName', 'rank']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_2_RANK',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                solventName: {
                                    rank: parseInt(value)
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_2_NAME',
                prop: ['solubility', 'data', '2', 'solventName', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_2_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                solventName: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_2_ENABLE',
                prop: ['solubility', 'data', '2', 'solventName', 'enable']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_2_ENABLE',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                solventName: {
                                    enable: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_2_VALUE_NAME',
                prop: ['solubility', 'data', '2', 'value', 'value', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_2_VALUE_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                value: {
                                    value: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_2_VALUE',
                prop: ['solubility', 'data', '2', 'value', 'value']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_2_VALUE',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                value: {
                                    value: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_2_VALUE_OPERATOR',
                prop: ['solubility', 'data', '2', 'value', 'operator', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_2_VALUE_OPERATOR',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                value: {
                                    operator: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_2_VALUE_UNIT',
                prop: ['solubility', 'data', '2', 'value', 'unit', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_2_VALUE_UNIT',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: {
                                value: {
                                    unit: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_3_COMMENT',
                prop: ['solubility', 'data', '3', 'comment']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_3_COMMENT',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                comment: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_3_TYPE',
                prop: ['solubility', 'data', '3', 'type', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_3_TYPE',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                type: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_3_RANK',
                prop: ['solubility', 'data', '3', 'solventName', 'rank']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_3_RANK',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                solventName: {
                                    rank: parseInt(value)
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_3_NAME',
                prop: ['solubility', 'data', '3', 'solventName', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_3_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                solventName: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_3_ENABLE',
                prop: ['solubility', 'data', '3', 'solventName', 'enable']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_3_ENABLE',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                solventName: {
                                    enable: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_3_VALUE_NAME',
                prop: ['solubility', 'data', '3', 'value', 'value', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_3_VALUE_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                value: {
                                    value: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_3_VALUE',
                prop: ['solubility', 'data', '3', 'value', 'value']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_3_VALUE',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                value: {
                                    value: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_3_VALUE_OPERATOR',
                prop: ['solubility', 'data', '3', 'value', 'operator', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_3_VALUE_OPERATOR',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                value: {
                                    operator: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_3_VALUE_UNIT',
                prop: ['solubility', 'data', '3', 'value', 'unit', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_3_VALUE_UNIT',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: {
                                value: {
                                    unit: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_4_COMMENT',
                prop: ['solubility', 'data', '4', 'comment']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_4_COMMENT',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                comment: value
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_4_TYPE',
                prop: ['solubility', 'data', '4', 'type', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_4_TYPE',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                type: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_4_RANK',
                prop: ['solubility', 'data', '4', 'solventName', 'rank']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_4_RANK',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                solventName: {
                                    rank: parseInt(value)
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_4_NAME',
                prop: ['solubility', 'data', '4', 'solventName', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_4_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                solventName: {
                                    name: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_4_ENABLE',
                prop: ['solubility', 'data', '4', 'solventName', 'enable']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_4_ENABLE',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                solventName: {
                                    enable: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_4_VALUE_NAME',
                prop: ['solubility', 'data', '4', 'value', 'value', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_4_VALUE_NAME',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                value: {
                                    value: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_4_VALUE',
                prop: ['solubility', 'data', '4', 'value', 'value']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_4_VALUE',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                value: {
                                    value: value
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_4_VALUE_OPERATOR',
                prop: ['solubility', 'data', '4', 'value', 'operator', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_4_VALUE_OPERATOR',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                value: {
                                    operator: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_4_VALUE_UNIT',
                prop: ['solubility', 'data', '4', 'value', 'unit', 'name']
            },
            import: {
                name: 'solubility',
                code: 'SOLUBILITY_SOLVENTS_4_VALUE_UNIT',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: {
                                value: {
                                    unit: {
                                        name: value
                                    }
                                }
                            }
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'EXTERNAL_SUPPLIER_STRING',
                prop: ['externalSupplier', 'asString']
            },
            import: {
                name: 'externalSupplier',
                code: 'EXTERNAL_SUPPLIER_STRING',
                format: function(dicts, value) {
                    return {
                        asString: value
                    };
                }
            }
        },
        {
            export: {
                name: 'EXTERNAL_SUPPLIER_REG_NUMBER',
                prop: ['externalSupplier', 'catalogRegistryNumber']
            },
            import: {
                name: 'externalSupplier',
                code: 'EXTERNAL_SUPPLIER_REG_NUMBER',
                format: function(dicts, value) {
                    return {
                        catalogRegistryNumber: value
                    };
                }
            }
        },
        {
            export: {
                name: 'EXTERNAL_SUPPLIER_CODE_AND_NAME',
                prop: ['externalSupplier', 'codeAndName', 'name']
            },
            import: {
                name: 'externalSupplier',
                code: 'EXTERNAL_SUPPLIER_CODE_AND_NAME',
                format: function(dicts, value) {
                    return {
                        codeAndName: {
                            name: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'COMPOUND_PROTECTION',
                prop: ['compoundProtection', 'name']
            },
            import: {
                name: 'compoundProtection',
                code: 'COMPOUND_PROTECTION',
                format: function(dicts, value) {
                    return {
                        name: value
                    };
                }
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_STRING',
                prop: ['healthHazards', 'asString']
            },
            import: {
                name: 'healthHazards',
                code: 'HEALTH_HAZARDS_STRING',
                format: function(dicts, value) {
                    return {
                        asString: value
                    };
                }
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_0',
                prop: ['healthHazards', 'data', '0']
            },
            import: {
                name: 'healthHazards',
                code: 'HEALTH_HAZARDS_0',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_1',
                prop: ['healthHazards', 'data', '1']
            },
            import: {
                name: 'healthHazards',
                code: 'HEALTH_HAZARDS_1',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_2',
                prop: ['healthHazards', 'data', '2']
            },
            import: {
                name: 'healthHazards',
                code: 'HEALTH_HAZARDS_2',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_3',
                prop: ['healthHazards', 'data', '3']
            },
            import: {
                name: 'healthHazards',
                code: 'HEALTH_HAZARDS_3',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_4',
                prop: ['healthHazards', 'data', '4']
            },
            import: {
                name: 'healthHazards',
                code: 'HEALTH_HAZARDS_4',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_5',
                prop: ['healthHazards', 'data', '5']
            },
            import: {
                name: 'healthHazards',
                code: 'HEALTH_HAZARDS_5',
                format: function(dicts, value) {
                    return {
                        data: {
                            5: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'HANDLING_PRECAUTIONS_STRING',
                prop: ['handlingPrecautions', 'asString']
            },
            import: {
                name: 'handlingPrecautions',
                code: 'HANDLING_PRECAUTIONS_STRING',
                format: function(dicts, value) {
                    return {
                        asString: value
                    };
                }
            }
        },
        {
            export: {
                name: 'HANDLING_PRECAUTIONS_0',
                prop: ['handlingPrecautions', 'data', '0']
            },
            import: {
                name: 'handlingPrecautions',
                code: 'HANDLING_PRECAUTIONS_0',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'HANDLING_PRECAUTIONS_1',
                prop: ['handlingPrecautions', 'data', '1']
            },
            import: {
                name: 'handlingPrecautions',
                code: 'HANDLING_PRECAUTIONS_1',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'HANDLING_PRECAUTIONS_2',
                prop: ['handlingPrecautions', 'data', '2']
            },
            import: {
                name: 'handlingPrecautions',
                code: 'HANDLING_PRECAUTIONS_2',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'HANDLING_PRECAUTIONS_3',
                prop: ['handlingPrecautions', 'data', '3']
            },
            import: {
                name: 'handlingPrecautions',
                code: 'HANDLING_PRECAUTIONS_3',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'HANDLING_PRECAUTIONS_4',
                prop: ['handlingPrecautions', 'data', '4']
            },
            import: {
                name: 'handlingPrecautions',
                code: 'HANDLING_PRECAUTIONS_4',
                format: function(dicts, value) {
                    return {
                        data: {
                            4: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'STORAGE_INSTRUCTIONS_STRING',
                prop: ['storageInstructions', 'asString']
            },
            import: {
                name: 'storageInstructions',
                code: 'STORAGE_INSTRUCTIONS_STRING',
                format: function(dicts, value) {
                    return {
                        asString: value
                    };
                }
            }
        },
        {
            export: {
                name: 'STORAGE_INSTRUCTIONS_0',
                prop: ['storageInstructions', 'data', '0']
            },
            import: {
                name: 'storageInstructions',
                code: 'STORAGE_INSTRUCTIONS_0',
                format: function(dicts, value) {
                    return {
                        data: {
                            0: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'STORAGE_INSTRUCTIONS_1',
                prop: ['storageInstructions', 'data', '1']
            },
            import: {
                name: 'storageInstructions',
                code: 'STORAGE_INSTRUCTIONS_1',
                format: function(dicts, value) {
                    return {
                        data: {
                            1: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'STORAGE_INSTRUCTIONS_2',
                prop: ['storageInstructions', 'data', '2']
            },
            import: {
                name: 'storageInstructions',
                code: 'STORAGE_INSTRUCTIONS_2',
                format: function(dicts, value) {
                    return {
                        data: {
                            2: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'STORAGE_INSTRUCTIONS_3',
                prop: ['storageInstructions', 'data', '3']
            },
            import: {
                name: 'storageInstructions',
                code: 'STORAGE_INSTRUCTIONS_3',
                format: function(dicts, value) {
                    return {
                        data: {
                            3: value
                        }
                    };
                }
            }
        },
        {
            export: {
                name: 'BATCH_COMMENTS',
                prop: ['comments']
            },
            import: {
                name: 'comments',
                code: 'BATCH_COMMENTS',
                format: function(dicts, value) {
                    return value;
                }
            }
        }
    ];

    return {
        constants: constants
    };

    function getItem(list, prop, value) {
        console.log('get item: ', _.find(list, function(item) {
            return item[prop].toUpperCase() === value.toUpperCase();
        })
        );

        return _.find(list, function(item) {
            return item[prop].toUpperCase() === value.toUpperCase();
        });
    }

    function getWord(dicts, dictName, prop, value) {
        var dict = _.find(dicts, function(dict) {
            return dict.name === dictName;
        });
        if (dict) {
            return getItem(dict.words, prop, value);
        }
    }
}
