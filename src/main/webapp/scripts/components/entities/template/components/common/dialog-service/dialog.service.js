angular.module('indigoeln')
    .factory('DialogService', function ($uibModal) {
        return {
            structureValidation: function (batches, searchQuery, callback) {
                $uibModal.open({
                    animation: true,
                    size: 'lg',
                    controller: 'StructureValidationController',
                    templateUrl: 'scripts/components/entities/template/components/common/dialog-service/structure-validation/structure-validation.html',
                    resolve: {
                        batches: function () {
                            return batches;
                        },
                        searchQuery: function () {
                            return searchQuery;
                        }
                    }
                }).result.then(function (result) {
                    callback(result);
                });
            }
        };
    });