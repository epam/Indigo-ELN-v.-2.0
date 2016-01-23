'use strict';

angular
    .module('indigoeln')
    .factory('experimentTablesService', function ($resource, DateUtils) {
        return $resource('api/experiments/tables', {}, {
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);

                    function transformDatesInTable(tableData) {
                        angular.forEach(tableData, function (exp, key) {
                            exp.creationDate = DateUtils.convertLocaleDateFromServer(exp.creationDate);
                            exp.lastEditDate = DateUtils.convertLocaleDateFromServer(exp.lastEditDate);
                        });
                    }

                    transformDatesInTable(data.openAndCompletedExp);
                    transformDatesInTable(data.waitingSignatureExp);
                    transformDatesInTable(data.submittedAndSigningExp);
                    return data;
                }
            }
        });
    });

