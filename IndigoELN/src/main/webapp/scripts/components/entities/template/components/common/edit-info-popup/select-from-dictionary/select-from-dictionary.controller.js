angular.module('indigoeln').controller('SelectFromDictionaryController',
    function($scope, $rootScope, $uibModalInstance, data, dictionary, title) {
        var vm = this;

        init();

        function init() {
            vm.title = title;
            vm.dictionary = dictionary;
            vm.model = data || {};
            vm.model.data = $scope.model.data || [];
            vm.selectedItemsFlags = getSelectedItems();

            vm.selectItem = selectItem;
            vm.save = save;
            vm.cancel = cancel;
        }

        function getSelectedItems() {
            return _.map(vm.dictionary.words, function(item) {
                return _.includes(vm.model.data, item.name);
            });
        }

        function selectItem(index, item) {
            if ($scope.selectedItemsFlags[index]) {
                $scope.model.data[index] = item;
            } else {
                delete $scope.model.data[index];
            }
        }

        function save() {
            $scope.model.data = [];
            _.each($scope.selectedItemsFlags, function(isSelected, index) {
                if (isSelected) {
                    $scope.model.data.push($scope.dictionary.words[index].name);
                }
            });
            $scope.model.asString = $scope.model.data.join(', ');
            $uibModalInstance.close($scope.model);
        }

        function cancel() {
            $uibModalInstance.dismiss('cancel');
        }
    });
