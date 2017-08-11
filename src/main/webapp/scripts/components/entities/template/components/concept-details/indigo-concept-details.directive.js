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
        function IndigoConceptDetailsController($state, $q, Principal, Dictionary, Users, notifyService) {
            var vm = this;
            var deferred;

            vm.experiment = vm.experiment || {};
            vm.model = vm.model || {};

            vm.onLinkedExperimentClick = onLinkedExperimentClick;
            vm.onAddLinkedExperiment = onAddLinkedExperiment;
            vm.getExperiments = getExperiments;

            init();

            function init() {
                Users.get().then(function(dictionary) {
                    vm.users = dictionary.words;
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
