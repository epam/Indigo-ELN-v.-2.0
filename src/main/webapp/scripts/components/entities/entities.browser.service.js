'use strict';

/**
 * Created by Stepan_Litvinov on 2/17/2016.
 */
angular.module('indigoeln')
    .factory('EntitiesBrowser', function (Experiment, Notebook, Project, $q, $state, Principal) {
        var tabs = {};
        var cache = {};
        var kindConf = {
            experiment: {
                service: Experiment,
                go: function (params) {
                    $state.go('entities.experiment-detail', params);
                }
            },
            notebook: {
                service: Notebook,
                go: function (params) {
                    $state.go('entities.notebook-detail', params);
                }
            },
            project: {
                service: Project,
                go: function (params) {
                    $state.go('entities.project-detail', params);
                }
            }
        };

        var getUserId = function() {
            var id = Principal.getIdentity().id;
            tabs[id] = tabs[id] || {};
            cache[id] = cache[id] || {};
            return id;
        }

        var resolvePrincipal = function (func) {
            return Principal.identity().then(function (){return func()});
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
                return {projectId: idsArr[0], notebookId: idsArr[1], experimentId: idsArr[2]};
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
                var that = this;
                return resolvePrincipal(function () {
                    var userId = getUserId();
                    tabs[userId][that.compactIds(params)] = that.resolveFromCache(params);
                    return $q.all(_.values(tabs[userId]));
                });
            },
            getCurrentEntity: function (params) {
                var that = this;
                return resolvePrincipal(function () {
                    var userId = getUserId();
                    return tabs[userId][that.compactIds(params)];
                });
            },
            getIdByVal: function (entity) {
                return resolvePrincipal(this, function () {
                    var userId = getUserId();
                    return Object.keys(tabs).filter(function (key) {
                        return tabs[userId][key] === entity.$promise;
                    })[0];
                });
            },
            goToTab: function (fullId) {
                var params = this.expandIds(fullId);
                kindConf[this.getKind(params)].go(params);
            },
            close: function (fullId, current) {
                var userId = getUserId();
                var keys = _.keys(tabs[userId]);
                if (keys.length > 1) {
                    var positionForClose = _.indexOf(keys, fullId);
                    var curPosition = _.indexOf(keys, current);
                    var nextKey;
                    if (curPosition === positionForClose) {
                        nextKey = keys[positionForClose - 1] || keys[positionForClose + 1];
                    } else {
                        nextKey = keys[curPosition];
                    }
                    delete tabs[userId][fullId];
                    delete cache[userId][fullId];
                    this.goToTab(nextKey);
                }
            },
            resolveFromCache: function (params) {
                var that = this;
                return resolvePrincipal( function () {
                    var userId = getUserId();
                    var entitiyId = that.compactIds(params);
                    if (!cache[userId][entitiyId]) {
                        cache[userId][entitiyId] = kindConf[that.getKind(params)].service.get(params).$promise;
                    }
                    return cache[userId][entitiyId];
                });
            },
            getTabs: function () {
                return resolvePrincipal(function() {
                    var userId = getUserId();
                    return $q.all(_.values(tabs[userId]));
                });
            },
            getNotebookFromCache: function (params) {
                var notebookParams = {projectId: params.projectId, notebookId: params.notebookId};
                return this.resolveFromCache(notebookParams);
            },
            getProjectFromCache: function (params) {
                var projectParams = {projectId: params.projectId};
                return this.resolveFromCache(projectParams);
            }
        };
    });