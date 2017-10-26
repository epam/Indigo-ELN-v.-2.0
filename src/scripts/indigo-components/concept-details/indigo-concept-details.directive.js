(function() {
    angular
        .module('indigoeln.Components')
        .directive('indigoConceptDetails', indigoConceptDetails);

    function indigoConceptDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/indigo-components/concept-details/concept-details.html',
            scope: {
                model: '=',
                experiment: '=',
                isReadonly: '=',
                onChanged: '&'
            },
            controller: IndigoConceptDetailsController,
            bindToController: true,
            controllerAs: 'vm'
        };

        /* @ngInject */
        function IndigoConceptDetailsController($scope, $state, $q, Dictionary, notifyService, Users) {
            var vm = this;
            var loadExperimentsPromise;
            var userPromise;
            vm.experiment = vm.experiment || {};
            vm.model = vm.model || {};

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
                $scope.$watch('vm.model.conceptDetails.experimentCreator', function() {
                    userPromise.then(function() {
                        vm.experimentCreator = _.find(vm.users, {id: vm.model.conceptDetails.experimentCreator});
                    });
                });

                $scope.$watch('vm.model.conceptDetails.coAuthors', function() {
                    userPromise.then(function() {
                        vm.coAuthors = Users.getUsersById(vm.model.conceptDetails.coAuthors);
                    });

                });

                $scope.$watch('vm.model.conceptDetails.designers', function() {
                    userPromise.then(function() {
                        vm.designers = Users.getUsersById(vm.model.conceptDetails.designers);
                    });
                });
            }

            function updateIds(property, selectedValues) {
                vm.model.conceptDetails[property] = _.map(selectedValues, 'id');
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

            function loadExperiments() {
                if (loadExperimentsPromise) {
                    return loadExperimentsPromise.promise;
                }

                loadExperimentsPromise = Dictionary.get({id: 'experiments'}, function(dictionary) {
                    loadExperimentsPromise.resolve(dictionary.words);
                });

                return loadExperimentsPromise;
            }

            function getExperiments(query) {
                return loadExperiments().then(function(experiments) {
                    return _
                        .chain(experiments)
                        .filter(function(experiment) {
                            return experiment.name.startsWith(query);
                        })
                        .map(function(experiment) {
                            return experiment.name;
                        })
                        .value();
                });
            }
        }
    }
})();
