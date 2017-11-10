SomethingDetailsController.$inject = ['$scope', 'usersService'];

function SomethingDetailsController($scope, usersService) {
    var vm = this;
    var userPromise;

    init();

    function init() {
        userPromise = usersService.get().then(function(dictionary) {
            vm.users = dictionary.words;
        });

        vm.updateIds = updateIds;

        bindEvents();
    }

    function bindEvents() {
        $scope.$watch('vm.componentData.experimentCreator', function() {
            userPromise.then(function() {
                vm.experimentCreator = _.find(vm.users, {id: vm.componentData.experimentCreator});
            });
        });

        $scope.$watch('vm.componentData.coAuthors', function() {
            userPromise.then(function() {
                vm.coAuthors = usersService.getUsersById(vm.componentData.coAuthors);
            });
        });

        $scope.$watch('vm.componentData.designers', function(designers) {
            if (designers) {
                userPromise.then(function() {
                    vm.designers = usersService.getUsersById(vm.componentData.designers);
                });
            }
        });

        $scope.$watch('vm.componentData.batchOwner', function(batchOwner) {
            if (batchOwner) {
                userPromise.then(function() {
                    vm.batchOwner = usersService.getUsersById(vm.componentData.batchOwner);
                });
            }
        });
    }

    function updateIds(property, selectedValues) {
        vm.componentData[property] = _.map(selectedValues, 'id');
    }
}

module.exports = SomethingDetailsController;
