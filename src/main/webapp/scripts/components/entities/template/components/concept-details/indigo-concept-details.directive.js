(function() {
    angular
        .module('indigoeln')
        .directive('indigoConceptDetails', indigoConceptDetails);

    function indigoConceptDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/concept-details/concept-details.html',
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
        function IndigoConceptDetailsController($scope, $state, $q, Dictionary, notifyService, componentsUtils, Users) {
            var vm = this;
            var deferred;
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
                        vm.coAuthors = componentsUtils.getUsersById(vm.model.conceptDetails.coAuthors, vm.users);
                    });

                });

                $scope.$watch('vm.model.conceptDetails.designers', function() {
                    userPromise.then(function() {
                        vm.designers = componentsUtils.getUsersById(vm.model.conceptDetails.designers, vm.users);
                    });
                });
            }

            function updateIds(property, selectedValues) {
                vm.model.conceptDetails[property] = _.map(selectedValues, function(value) {
                    return value.id;
                });
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
                var _deferred = $q.defer();

                loadExperiments().then(function(experiments) {
                    _deferred.resolve(_.isObject(_.find(experiments, function(experiment) {
                        return experiment.name === tag.text;
                    })));
                });
                return _deferred.promise;
            }

            function loadExperiments() {
                if (deferred) {
                    return deferred.promise;
                }

                deferred = $q.defer();
                Dictionary.get({id: 'experiments'}, function(dictionary) {
                    deferred.resolve(dictionary.words);
                });

                return deferred.promise;
            }

            function getExperiments(query) {
                var _deferred = $q.defer();
                loadExperiments().then(function(experiments) {
                    var filtered = _.chain(experiments).filter(function(experiment) {
                        return experiment.name.startsWith(query);
                    }).map(function(experiment) {
                        return experiment.name;
                    }).value();
                    _deferred.resolve(filtered);
                });
                return _deferred.promise;
            }
        }
    }
})();
