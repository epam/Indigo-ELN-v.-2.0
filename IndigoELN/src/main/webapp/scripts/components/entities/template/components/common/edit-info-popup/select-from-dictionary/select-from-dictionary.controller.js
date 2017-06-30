angular.module('indigoeln').controller('SelectFromDictionaryController',
    function($scope, $rootScope, $uibModalInstance, data, dictionary, title) {
        var vm = this;

        init();

        function init() {
            vm.title = title;
            vm.dictionary = dictionary;
            vm.model = data || {};
            vm.model.data = vm.model.data || [];
            vm.selectedItemsFlags = getSelectedItems();

            vm.selectItem = selectItem;
            vm.save = save;
            vm.cancel = cancel;
        }

        function getSelectedItems() {
            return _.map(vm.dictionary.words, function(item) {
                return _.contains(vm.model.data, item.name);
            });
        }

        function selectItem(index, item) {
            if (vm.selectedItemsFlags[index]) {
                vm.model.data[index] = item;
            } else {
                delete vm.model.data[index];
            }
        }

        function save() {
            vm.model.data = [];
            _.each(vm.selectedItemsFlags, function(isSelected, index) {
                if (isSelected) {
                    vm.model.data.push(vm.dictionary.words[index].name);
                }
            });
            vm.model.asString = vm.model.data.join(', ');
            $uibModalInstance.close(vm.model);
        }

        function cancel() {
            $uibModalInstance.dismiss('cancel');
        }
    });
