(function() {
    angular
        .module('indigoeln.Components')
        .controller('SomethingDetailsController', SomethingDetailsController);

    /* @ngInject */
    function SomethingDetailsController($scope, Users, componentHelper) {
        var vm = this;
        var userPromise;

        init();

        function init() {
            userPromise = Users.get().then(function(dictionary) {
                vm.users = dictionary.words;
            });

            vm.onLinkedExperimentClick = componentHelper.onLinkedExperimentClick;
            vm.onAddLinkedExperiment = componentHelper.onAddLinkedExperiment;
            vm.getExperiments = componentHelper.getExperiments;
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
                    vm.coAuthors = Users.getUsersById(vm.componentData.coAuthors);
                });
            });

            $scope.$watch('vm.componentData.designers', function(designers) {
                if (designers) {
                    userPromise.then(function() {
                        vm.designers = Users.getUsersById(vm.componentData.designers);
                    });
                }
            });

            $scope.$watch('vm.componentData.batchOwner', function(batchOwner) {
                if (batchOwner) {
                    userPromise.then(function() {
                        vm.batchOwner = Users.getUsersById(vm.componentData.batchOwner);
                    });
                }
            });
        }

        function updateIds(property, selectedValues) {
            vm.componentData[property] = _.map(selectedValues, 'id');
        }
    }
})();
