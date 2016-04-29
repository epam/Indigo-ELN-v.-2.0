angular.module('indigoeln').controller('EditPurityController',
    function ($scope, $rootScope, $uibModalInstance, data, dictionary) {
        $scope.purity = data || {};
        $scope.purity.data = $scope.purity.data || [];
        $scope.dictionary = dictionary;

        $scope.isDisabled = function () {
            return $scope.purity.property === 'Purity Unknown';
        };
        $scope.operatorSelect = [
            {name: '>'},
            {name: '<'},
            {name: '='},
            {name: '~'}];

        var resultToString = function () {
            var purityStrings = _.map($scope.purity.data, function(purity) {
                if (purity.operator && purity.value && purity.comments) {
                    return purity.determinedBy + ' purity ' + purity.operator.name + ' ' +
                        purity.value + '% ' + purity.comments;
                } else if (purity.operator && purity.value) {
                    return purity.determinedBy + ' purity ' + purity.operator.name + ' ' +
                        purity.value + '%';
                } else {
                    return '';
                }
            });
            return _.compact(purityStrings).join(', ');
        };

        $scope.save = function () {
            $scope.purity.asString = resultToString();
            $uibModalInstance.close($scope.purity);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });
