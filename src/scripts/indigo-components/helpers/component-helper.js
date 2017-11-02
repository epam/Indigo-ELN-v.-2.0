(function() {
    angular.module('indigoeln')
        .factory('componentHelper', componentHelper);

    componentHelper.$inject = ['Dictionary', 'notifyService', '$state'];

    function componentHelper(Dictionary, notifyService, $state) {
        var loadExperimentsPromise;
        var experimentsPromise;

        return {
            onAddLinkedExperiment: onAddLinkedExperiment,
            getExperiments: getExperiments,
            onLinkedExperimentClick: onLinkedExperimentClick
        };

        function onAddLinkedExperiment(tag) {
            return loadExperiments().then(function(experiments) {
                return _.isObject(_.find(experiments, function(experiment) {
                    return experiment.name === tag.text;
                }));
            });
        }

        function loadExperiments() {
            if (loadExperimentsPromise) {
                return loadExperimentsPromise;
            }

            loadExperimentsPromise = Dictionary.get({id: 'experiments'}).$promise.then(function(dictionary) {
                return dictionary.words;
            });

            return loadExperimentsPromise;
        }

        function getExperiments() {
            if (experimentsPromise) {
                return experimentsPromise;
            }

            experimentsPromise = loadExperiments();

            return experimentsPromise;
        }

        function onLinkedExperimentClick(tag) {
            return loadExperiments().then(function(experiments) {
                var foundExperiment = _.find(experiments, function(experiment) {
                    return experiment.name === tag.text;
                });
                if (!foundExperiment) {
                    notifyService.error('Can not find a experiment with the name: ' + tag.text);

                    return;
                }
                $state.go('entities.experiment-detail', {
                    experimentId: foundExperiment.id,
                    notebookId: foundExperiment.notebookId,
                    projectId: foundExperiment.projectId
                });
            });
        }
    }
})();
