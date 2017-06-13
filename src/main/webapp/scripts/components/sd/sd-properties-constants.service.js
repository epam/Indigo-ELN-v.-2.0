angular.module('indigoeln')
    .constant('sdPropertiesConstants', {
            registrationStatus      :       {   export : {  name: 'REGISTRATION_STATUS',
                                                            prop: 'registrationStatus',
                                                            subProp: undefined},
                                                import : {  name: 'registrationStatus',
                                                            code: 'REGISTRATION_STATUS',
                                                            format: undefined}
                        },
            conversationalBatchNumber :     {   export : {  name: 'CONVERSATIONAL_BATCH_NUMBER',
                                                            prop: 'conversationalBatchNumber',
                                                            subProp: undefined},
                                                import : {}
                        },
            virtualCompoundId :             {   export : {  name: 'VIRTUAL_COMPOUND_ID',
                                                            prop: 'virtualCompoundId',
                                                            subProp: undefined},
                                                import : {}
                        },
            source :                        {   export : {  name: 'COMPOUND_SOURCE_CODE',
                                                            prop: 'source',
                                                            subProp: 'name'},
                                                import : {}
                        },
            sourceDetail :                  {   export : {  name: 'COMPOUND_SOURCE_DETAIL_CODE',
                                                            prop: 'sourceDetail',
                                                            subProp: 'name'},
                                                import : {}
                        },
            stereoisomer :                  {   export : {  name: 'STEREOISOMER_CODE',
                                                            prop: 'stereoisomer',
                                                            subProp: 'name'},
                                                import : {}
                        },
            saltCode :                      {   export : {  name: 'GLOBAL_SALT_CODE',
                                                            prop: 'saltCode',
                                                            subProp: 'regValue'},
                                                import : {}
                        },
            saltEq :                        {   export : {  name: 'GLOBAL_SALT_EQ',
                                                            prop: 'saltEq',
                                                            subProp: 'value'},
                                                import : {}
                        },
            structureComments :             {   export : {  name: 'STRUCTURE_COMMENT',
                                                            prop: 'structureComments',
                                                            subProp: undefined},
                                                import : {}
                        },
            compoundState :                 {   export : {  name: 'COMPOUND_STATE',
                                                            prop: 'compoundState',
                                                            subProp: 'name'},
                                                import : {}
                        },
            precursors :                    {   export : {  name: 'PRECURSORS',
                                                            prop: 'precursors',
                                                            subProp: undefined},
                                                import : {}
                        }
            //TODO:

    });