angular
    .module('indigoeln')
    .factory('LinkedExperimentUtils', linkedExperimentUtils);

/* @ngInject */
function linkedExperimentUtils(AllNotebooks, AllExperiments, Alert, $state, Project) {
    return {
        onLinkedExperimentClick: onLinkedExperimentClick
    };

    function onLinkedExperimentClick(tag) {
        var names = tag.text.split('-');
        var notebookName = names[0];
        var experimentName = names[1];

        if (!notebookName || !experimentName) {
            Alert.error('Wrong experiment name: ' + tag.text);

            return;
        }
        var currentProject = Project.get($state.params);
        currentProject.then(function(project) {
            AllNotebooks.query({
                projectId: project.id
            }, function(notebooks) {
                var notebook = _.find(notebooks, {
                    name: notebookName
                });
                if (!notebook) {
                    Alert.error('Can not find a notebook with the name: ' + notebookName);

                    return;
                }
                AllExperiments.query({
                    projectId: project.id,
                    notebookId: notebook.id
                }, function(experiments) {
                    var experiment = _.find(experiments, function(experiment) {
                        return experiment.name === experimentName || experiment.name.startsWith(experimentName + ' ');
                    });
                    if (!experiment) {
                        Alert.error('Can not find a experiment with the name: ' + experimentName);

                        return;
                    }
                    $state.go('entities.experiment-detail', {
                        experimentId: experiment.id,
                        notebookId: notebook.id,
                        projectId: project.id
                    });
                });
            });
        });
    }
}
