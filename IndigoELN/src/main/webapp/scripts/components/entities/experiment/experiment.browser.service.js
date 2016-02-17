'use strict';

/**
 * Created by Stepan_Litvinov on 2/17/2016.
 */
angular.module('indigoeln')
    .factory('ExperimentBrowser', function (Experiment, $q, $rootScope) {
        var experiments = {};
        return {
            compactIds: function (params) {
                return params.projectId + '-' + params.notebookId + '-' + params.experimentId;
            },
            expandIds: function (ids) {
                var idsArr = ids.split('-');
                return {projectId: idsArr[0], notebookId: idsArr[1], experimentId: idsArr[2]}
            },
            resolveExperiments: function (experimentId) {
                if (!experiments[experimentId]) {
                    experiments[experimentId] = Experiment.get(this.expandIds(experimentId)).$promise;
                }
                return $q.all(_.values(experiments));
            },
            getIdByVal: function (experiment) {
                return Object.keys(experiments).filter(function (key) {
                    return experiments[key] === experiment.$promise
                })[0];
            },
            close: function ($state, compactId, current) {
                var keys = _.keys(experiments);
                if (keys.length > 1) {
                    var positionForClose = _.indexOf(keys, compactId);
                    var curPosition = _.indexOf(keys, current);
                    var nextKey;
                    if (curPosition == positionForClose) {
                        nextKey = keys[positionForClose - 1] || keys[positionForClose + 1];
                    } else {
                        nextKey = keys[curPosition];
                    }
                    delete experiments[compactId];
                    $state.go('experiment.detail', {experimentId: nextKey}, {reload: true})
                }
            }
        };
    });