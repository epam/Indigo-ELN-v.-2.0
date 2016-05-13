angular.module('indigoeln').controller('SelectFromDictionaryController',
    function ($scope, $rootScope, $uibModalInstance, data, dictionary, title) {
        $scope.title = title;
        $scope.dictionary = dictionary;
        $scope.model = data || {};
        $scope.model.data = $scope.model.data || [];
        $scope.selectedItemsFlags = _.map($scope.dictionary.words, function(item) {
            if (_.contains($scope.model.data, item.name)) {
                return true;
            }
        });

        $scope.selectItem = function (index, item) {
            if ($scope.selectedItemsFlags[index]) {
                $scope.model.data[index] = item;
            } else {
                delete $scope.model.data[index];
            }
        };

        $scope.save = function () {
            $scope.model.data = [];
            _.each($scope.selectedItemsFlags, function(isSelected, index) {
                if (isSelected) {
                    $scope.model.data.push($scope.dictionary.words[index].name);
                }
            });
            $scope.model.asString = $scope.model.data.join(', ');
            $uibModalInstance.close($scope.model);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });
