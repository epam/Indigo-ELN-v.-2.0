angular.module('indigoeln')
    .factory('sdProperties', function(AppValues) {

        var getItem = function (list, prop, value) {
        console.log('get item: ', _.find(list, function (item) {return item[prop].toUpperCase() === value.toUpperCase();})
                    );
            return _.find(list, function (item) {
                return item[prop].toUpperCase() === value.toUpperCase();
            });
        };

        var getWord = function (dicts, dictName, prop, value) {
            var dict = _.find(dicts, function (dict) {
                return dict.name === dictName;
            });
            if (dict) {
                return getItem(dict.words, prop, value);
            }
        };

        var constants = [
//            {   export : {  name: 'REGISTRATION_STATUS',
//                            prop: 'registrationStatus',
//                            subProp: undefined},
//                import : {  name: 'registrationStatus',
//                            code: 'REGISTRATION_STATUS',
//                            format: undefined}
//            },
//            {   export : {  name: 'CONVERSATIONAL_BATCH_NUMBER',
//                            prop: 'conversationalBatchNumber',
//                            subProp: undefined},
//                import : {  name: 'conversationalBatchNumber',
//                            code: 'CONVERSATIONAL_BATCH_NUMBER',
//                            format: undefined}
//            },
//            {   export : {  name: 'VIRTUAL_COMPOUND_ID',
//                            prop: 'virtualCompoundId',
//                            subProp: undefined},
//                import : {  name: 'virtualCompoundId',
//                            code: 'VIRTUAL_COMPOUND_ID',
//                            format: undefined}
//            },
//            {   export : {  name: 'COMPOUND_SOURCE_CODE',
//                            prop: 'source',
//                            subProp: 'name'},
//                import : {  name: 'source',
//                            code: 'COMPOUND_SOURCE_CODE',
//                            format: function (dicts, value) {
//                                        return getWord(dicts, 'Source', 'name', value);
//                                    }}
//            },
//            {   export : {  name: 'COMPOUND_SOURCE_DETAIL_CODE',
//                            prop: 'sourceDetail',
//                            subProp: 'name'},
//                import : {  name: 'sourceDetail',
//                            code: 'COMPOUND_SOURCE_DETAIL_CODE',
//                            format: function (dicts, value) {
//                                        return getWord(dicts, 'Source Details', 'name', value);
//                                    }}
//            },
//            {   export : {  name: 'STEREOISOMER_CODE',
//                            prop: 'stereoisomer',
//                            subProp: 'name'},
//                import : {  name: 'stereoisomer',
//                            code: 'STEREOISOMER_CODE',
//                            format: function (dicts, value) {
//                                        return getWord(dicts, 'Stereoisomer Code', 'name', value);
//                                     }}
//            },
//            {   export : {  name: 'GLOBAL_SALT_CODE',
//                            prop: 'saltCode',
//                            subProp: 'regValue'},
//                import : {  name: 'saltCode',
//                            code: 'GLOBAL_SALT_CODE',
//                            format: function (dicts, value) {
//                                        return getItem(AppValues.getSaltCodeValues(), 'regValue', value);
//                                     }}
//            },
//            {   export : {  name: 'GLOBAL_SALT_EQ',
//                            prop: 'saltEq',
//                            subProp: 'value'},
//                import : {  name: 'saltEq',
//                            code: 'GLOBAL_SALT_EQ',
//                            format: function (dicts, value) {
//                                         return {
//                                             value: parseInt(value),
//                                             entered: false
//                                         };
//                                     }}
//            },
//            {   export : {  name: 'STRUCTURE_COMMENT',
//                            prop: 'structureComments',
//                            subProp: undefined},
//                import : {  name: 'structureComments',
//                            code: 'STRUCTURE_COMMENT',
//                            format: undefined}
//            },
//            {   export : {  name: 'COMPOUND_STATE',
//                            prop: 'compoundState',
//                            subProp: 'name'},
//                import : {  name: 'compoundState',
//                            code: 'COMPOUND_STATE',
//                            format: function (dicts, value) {
//                                        return getWord(dicts, 'Compound State', 'name', value);
//                                    }}
//            },
//            {   export : {  name: 'PRECURSORS',
//                            prop: 'precursors',
//                            subProp: undefined},
//                import : {  name: 'precursors',
//                            code: 'PRECURSORS',
//                            format: undefined}
//            },
            {
                export : {name: 'PURITY_0_COMENTS',
                          prop: ['purity', 'data', '0', 'comments'],
                          subProp: undefined},
                import : {name: 'purity',
                          code: 'PURITY_0_COMENTS',
                          format: function (dicts, value) {
                                       return {
                                           value: value
                                       };
                                   }
                          }
            },
            {
                export : {name: 'PURITY_0_DETERMINED',
                          prop: ['purity', 'data', '0', 'determinedBy'],
                          subProp: ''},
                import : {name: 'purity1',
                          code: 'PURITY_0_DETERMINED',
                          format: function (dicts, value) {
                                       return {
                                           value: value
                                       };
                                   }
                          }
            },
            {
                export : {name: 'PURITY_0_OPERATOR_NAME',
                          prop: ['purity', 'data', '0', 'operator', 'name'],
                          subProp: ''},
                import : {}
            },
            {
                export : {name: 'PURITY_0_VALUE',
                          prop: ['purity', 'data', '0', 'value'],
                          subProp: ''},
                import : {}
            },
            {
                export : {name: 'PURITY_1_COMENTS',
                          prop: ['purity', 'data', '1', 'comments'],
                          subProp: undefined},
                import : {}
            }//,
//            {
//                export : {name: 'PURITY_1_DETERMINED',
//                          prop: ['purity', 'data', '1', 'determinedBy'],
//                          subProp: ''},
//                import : {}
//            },
//            {
//                export : {name: 'PURITY_1_OPERATOR_NAME',
//                          prop: ['purity', 'data', '1', 'operator', 'name'],
//                          subProp: ''},
//                import : {}
//            },
//            {
//                export : {name: 'PURITY_1_VALUE',
//                          prop: ['purity', 'data', '1', 'value'],
//                          subProp: ''},
//                import : {}
//            },
//            {
//                export : {name: 'PURITY_2_COMENTS',
//                          prop: ['purity', 'data', '2', 'comments'],
//                          subProp: undefined},
//                import : {}
//            },
//            {
//                export : {name: 'PURITY_2_DETERMINED',
//                          prop: ['purity', 'data', '2', 'determinedBy'],
//                          subProp: ''},
//                import : {}
//            },
//            {
//                export : {name: 'PURITY_2_OPERATOR_NAME',
//                          prop: ['purity', 'data', '2', 'operator', 'name'],
//                          subProp: ''},
//                import : {}
//            },
//            {
//                export : {name: 'PURITY_2_VALUE',
//                          prop: ['purity', 'data', '2', 'value'],
//                          subProp: ''},
//                import : {}
//            }

            //TODO:
        ];

        return {
            constants: constants
        };

    });