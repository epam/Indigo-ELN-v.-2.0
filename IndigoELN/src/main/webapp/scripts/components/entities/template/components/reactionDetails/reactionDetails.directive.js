/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
angular.module('indigoeln')
    .directive('reactionDetails', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/reactionDetails/reactionDetails.html',
            controller: function ($scope, Principal, Dictionary, Alert, $state) {
                $scope.model.reactionDetails = $scope.model.reactionDetails || {};
                $scope.model.reactionDetails.experimentCreator = $scope.model.reactionDetails.experimentCreator ||
                    {name: Principal.getIdentity().fullName};

                Dictionary.get({id: 'users'}, function (dictionary) {
                    $scope.users = dictionary.words;
                    $scope.model.reactionDetails.batchOwner = $scope.model.reactionDetails.batchOwner ||
                        _.where($scope.users, {name: Principal.getIdentity().fullName});
                });
                $scope.onLinkedExperimentClick = function (tag) {
                    var experiment = _.find($scope.experiments, function (experiment) {
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
                };
                $scope.onAddLinkedExperiment = function (tag) {
                    return _.isObject(_.find($scope.experiments, function (experiment) {
                        return experiment.name === tag.text;
                    }));
                };
                $scope.getExperiments = function (query) {
                    return _.chain($scope.experiments).filter(function (experiment) {
                        return experiment.name.startsWith(query);
                    }).map(function (experiment) {
                        return experiment.name;
                    }).value();
                };
                Dictionary.get({id: 'experiments'}, function (dictionary) {
                    $scope.experiments = dictionary.words;
                });

            }
        };
    });