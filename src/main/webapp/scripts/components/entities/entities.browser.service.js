'use strict';

/**
 * Created by Stepan_Litvinov on 2/17/2016.
 */
angular.module('indigoeln')
    .factory('EntitiesBrowser', function (Experiment, Notebook, Project, $q, $state) {
        var tabs = {};
        var cache = {};
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
            resolveTabs: function (params) {
                tabs[this.compactIds(params)] = this.resolveFromCache(params)
                return $q.all(_.values(tabs));
            },
            getCurrentEntity: function (params) {
                return tabs[this.compactIds(params)];
            },
            getIdByVal: function (entitiy) {
                return Object.keys(tabs).filter(function (key) {
                    return tabs[key] === entitiy.$promise
                })[0];
            },
            goToTab: function (fullId) {
                var params = this.expandIds(fullId);
                kindConf[this.getKind(params)].go(params);
            },
            close: function (fullId, current) {
                var keys = _.keys(tabs);
                if (keys.length > 1) {
                    var positionForClose = _.indexOf(keys, fullId);
                    var curPosition = _.indexOf(keys, current);
                    var nextKey;
                    if (curPosition == positionForClose) {
                        nextKey = keys[positionForClose - 1] || keys[positionForClose + 1];
                    } else {
                        nextKey = keys[curPosition];
                    }
                    delete tabs[fullId];
                    delete cache[fullId];
                    this.goToTab(nextKey);
                }
            },
            resolveFromCache: function (params) {
                var entitiyId = this.compactIds(params);
                if (!cache[entitiyId]) {
                    cache[entitiyId] = kindConf[this.getKind(params)].service.get(params).$promise;
                }
                return cache[entitiyId];
            }
        };
    });