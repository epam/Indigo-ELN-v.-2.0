'use strict';

/**
 * Created by Stepan_Litvinov on 2/17/2016.
 */
angular.module('indigoeln')
    .factory('EntitiesBrowser', function (Experiment, Notebook, Project, $q, $state) {
        var entities = {};
        var kindConf = {
            experiment: {
                service: Experiment,
                go: function (params) {
                    $state.go('entities.experiment-detail', params, {reload: true})
                }
            },
            notebook: {
                service: Notebook,
                go: function (params) {
                    $state.go('entities.notebook-detail', params, {reload: true})
                }
            },
            project: {
                service: Project,
                go: function (params) {
                    $state.go('entities.project-detail', params, {reload: true})
                }
            }
        };
        return {
            compactIds: function (params) {
                var paramsArr = [];
                if (params.projectId) {
                    paramsArr.push(params.projectId);
                }
                if (params.notebookId) {
                    paramsArr.push(params.notebookId);
                }
                if (params.experimentId) {
                    paramsArr.push(params.experimentId);
                }
                return paramsArr.join('-');
            },
            expandIds: function (ids) {
                var idsArr = ids.split('-');
                return {projectId: idsArr[0], notebookId: idsArr[1], experimentId: idsArr[2]}
            },
            getKind: function (params) {
                if (params.experimentId) {
                    return 'experiment';
                } else if (params.notebookId) {
                    return 'notebook';
                } else {
                    return 'project';
                }
            },
            resolve: function (params) {
                var entitiyId = this.compactIds(params);
                if (!entities[entitiyId]) {
                    entities[entitiyId] = kindConf[this.getKind(params)].service.get(params).$promise;
                }
                return $q.all(_.values(entities));
            },
            getCurrentEntity: function (params) {
                return entities[this.compactIds(params)];
            },
            getIdByVal: function (entitiy) {
                return Object.keys(entities).filter(function (key) {
                    return entities[key] === entitiy.$promise
                })[0];
            },
            goToTab: function (fullId) {
                var params = this.expandIds(fullId);
                kindConf[this.getKind(params)].go(params);
            },
            close: function (fullId, current) {
                var keys = _.keys(entities);
                if (keys.length > 1) {
                    var positionForClose = _.indexOf(keys, fullId);
                    var curPosition = _.indexOf(keys, current);
                    var nextKey;
                    if (curPosition == positionForClose) {
                        nextKey = keys[positionForClose - 1] || keys[positionForClose + 1];
                    } else {
                        nextKey = keys[curPosition];
                    }
                    delete entities[fullId];
                    this.goToTab(nextKey);
                }
            }
        };
    });