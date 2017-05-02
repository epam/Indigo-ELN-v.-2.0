/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
angular.module('indigoeln')
    .directive('conceptDetails', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/conceptDetails/conceptDetails.html',
            controller: function ($scope, Principal, Dictionary, Users, Alert, $state, $q) {
                $scope.model.conceptDetails = $scope.model.conceptDetails || {};
                $scope.model.reactionDetails = $scope.model.reactionDetails || {};
                $scope.model.conceptDetails.experimentCreator = $scope.model.conceptDetails.experimentCreator ||
                    {name: Principal.getIdentity().fullName};
                $scope.onLinkedExperimentClick = function (tag) {
                    init().then(function(experiments) {
                        var experiment = _.find(experiments, function (experiment) {
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

                    })
                };
                $scope.onAddLinkedExperiment = function (tag) {
                     var _deferred = $q.defer();
                    init().then(function(experiments) {
                        _deferred.resolve(_.isObject(_.find(experiments, function(experiment) {
                            return experiment.name === tag.text;
                        })));
                    })
                    return _deferred.promise;
                };

                var deferred;
                function init() {
                    if (deferred) return deferred.promise;
                    deferred = $q.defer();
                    Dictionary.get({id: 'experiments'}, function (dictionary) {
                        deferred.resolve(dictionary.words)
                        console.log('inited', dictionary.words)
                    });
                    return deferred.promise;
                }
                Users.get().then(function(dictionary) {
                    $scope.users = dictionary.words;
                })
                $scope.getExperiments = function(query) {
                    var _deferred = $q.defer();
                    init().then(function(experiments) {
                        var filtered = _.chain(experiments).filter(function(experiment) {
                            return experiment.name.startsWith(query);
                        }).map(function(experiment) {
                            return experiment.name;
                        }).value();
                        _deferred.resolve(filtered)
                    })
                    return _deferred.promise;
                };
            }
        };
    });