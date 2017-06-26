(function() {
    angular
        .module('indigoeln')
        .controller('IndigoReactionDetailsController', IndigoReactionDetailsController);

    /* @ngInject */
    function IndigoReactionDetailsController($scope, Principal, Dictionary, Users, Alert, $state, $q) {
        $scope.model.reactionDetails = $scope.model.reactionDetails || {};
        $scope.model.reactionDetails.experimentCreator = $scope.model.reactionDetails.experimentCreator ||
            {
                name: Principal.getIdentity().fullName
            };

        Users.get().then(function(dictionary) {
            $scope.users = dictionary.words;
            $scope.model.reactionDetails.batchOwner = $scope.model.reactionDetails.batchOwner ||
                _.where($scope.users, {
                    name: Principal.getIdentity().fullName
                });
        });
        var deferred;

        function init() {
            if (deferred) {
                return deferred.promise;
            }

            deferred = $q.defer();
            Dictionary.get({
                id: 'experiments'
            }, function(dictionary) {
                deferred.resolve(dictionary.words);
                console.log('inited', dictionary.words);
            });

            return deferred.promise;
        }

        $scope.onLinkedExperimentClick = function(tag) {
            init().then(function(experiments) {
                var experiment = _.find(experiments, function(experiment) {
                    return experiment.name === tag.text;
                });
                if (!experiment) {
                    Alert.error('Can not find a experiment with the name: ' + tag.text);

                    return;
                }
                $state.go('entities.experiment-detail', {
                    experimentId: experiment.id,
                    notebookId: experiment.notebookId,
                    projectId: experiment.projectId
                });
            });
        };
        $scope.onAddLinkedExperiment = function(tag) {
            var _deferred = $q.defer();
            init().then(function(experiments) {
                _deferred.resolve(_.isObject(_.find(experiments, function(experiment) {
                    return experiment.name === tag.text;
                })));
            });

            return _deferred.promise;
        };

        $scope.getExperiments = function(query) {
            var _deferred = $q.defer();
            init().then(function(experiments) {
                var filtered = _.chain(experiments).filter(function(experiment) {
                    return experiment.name.startsWith(query);
                }).map(function(experiment) {
                    return experiment.name;
                }).value();
                _deferred.resolve(filtered);
            });

            return _deferred.promise;
        };
    }
})();
