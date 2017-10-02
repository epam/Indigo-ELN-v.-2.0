(function() {
    angular
        .module('indigoeln')
        .directive('indigoReactionDetails', indigoReactionDetails);

    function indigoReactionDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/reaction-details/reaction-details.html',
            controller: indigoReactionDetailsController,
            controllerAs: 'vm',
            bindToController: true,
            scope: {
                model: '=',
                experiment: '=',
                isReadonly: '='
            }
        };

        /* @ngInject */
        function indigoReactionDetailsController($scope, $state, $q, Dictionary, notifyService, Users) {
            var vm = this;
            var promiseLoadExperiments;
            var userPromise;

            vm.model = vm.model || {};
            vm.model.reactionDetails = vm.model.reactionDetails || {};

            vm.onLinkedExperimentClick = onLinkedExperimentClick;
            vm.onAddLinkedExperiment = onAddLinkedExperiment;
            vm.getExperiments = getExperiments;
            vm.updateIds = updateIds;

            init();

            function init() {
                userPromise = Users.get().then(function(dictionary) {
                    vm.users = dictionary.words;
                });

                bindEvents();
            }

            function bindEvents() {
                $scope.$watch('vm.model.reactionDetails.experimentCreator', function() {
                    userPromise.then(function() {
                        vm.experimentCreator = _.find(vm.users, {id: vm.model.reactionDetails.experimentCreator});
                    });
                });

                $scope.$watch('vm.model.reactionDetails.batchOwner', function() {
                    userPromise.then(function() {
                        vm.batchOwner = Users.getUsersById(vm.model.reactionDetails.batchOwner);
                    });

                });

                $scope.$watch('vm.model.reactionDetails.coAuthors', function() {
                    userPromise.then(function() {
                        vm.coAuthors = Users.getUsersById(vm.model.reactionDetails.coAuthors);
                    });
                });
            }

            function updateIds(property, selectedValues) {
                vm.model.reactionDetails[property] = _.map(selectedValues, 'id');
            }

            function onLinkedExperimentClick(tag) {
                loadExperiments().then(function(experiments) {
                    var experiment = _.find(experiments, function(experiment) {
                        return experiment.name === tag.text;
                    });
                    if (!experiment) {
                        notifyService.error('Can not find a experiment with the name: ' + tag.text);

                        return;
                    }
                    $state.go('entities.experiment-detail', {
                        experimentId: experiment.id,
                        notebookId: experiment.notebookId,
                        projectId: experiment.projectId
                    });
                });
            }

            function onAddLinkedExperiment(tag) {
                return loadExperiments().then(function(experiments) {
                    return _.isObject(_.find(experiments, function(experiment) {
                        return experiment.name === tag.text;
                    }));
                });
            }

            function getExperiments(query) {
                return loadExperiments().then(function(experiments) {
                    return _.chain(experiments).filter(function(experiment) {
                        return experiment.name.startsWith(query);
                    }).map(function(experiment) {
                        return experiment.name;
                    }).value();
                });
            }

            function loadExperiments() {
                if (promiseLoadExperiments) {
                    return promiseLoadExperiments;
                }

                promiseLoadExperiments = Dictionary.get({
                    id: 'experiments'
                }).promise.then(function(dictionary) {
                    return dictionary.words;
                });

                return promiseLoadExperiments;
            }
        }
    }
})();
