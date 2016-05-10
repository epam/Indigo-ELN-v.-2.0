'use strict';

angular.module('indigoeln').controller('SelectFromDictionaryController',
    function ($scope, $rootScope, $uibModalInstance, data, dictionary, title) {
        $scope.title = title;
        $scope.dictionary = dictionary;
        $scope.selectedItems = data || [];
        $scope.isSelected = _.map($scope.dictionary.words, function(item) {
            if (_.contains($scope.selectedItems, item.name)) {
                return true;
            }
        });

        $scope.selectItem = function (index, item) {
            $scope.isSelected[index] = !$scope.isSelected[index];
            if ($scope.isSelected[index]) {
                $scope.selectedItems[index] = item;
            } else {
                delete $scope.selectedItems[index];
            }
        };

        $scope.save = function () {
            $scope.selectedItems = [];
            _.each($scope.isSelected, function(isSelected, index) {
                if (isSelected) {
                    $scope.selectedItems.push($scope.dictionary.words[index].name);
                }
            });
            $uibModalInstance.close($scope.selectedItems);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });
