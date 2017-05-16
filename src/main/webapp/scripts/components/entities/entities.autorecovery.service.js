angular.module('indigoeln')
    .factory('AutoRecoverEngine', function($rootScope, AlertModal, Principal, localStorageService, $q, $timeout) {
        var delay = 1000;
        var servfields = ['lastModifiedBy', 'lastVersion', 'version', 'lastEditDate', 'creationDate', 'templateContent']
        var kinds = ['experiment', 'project', 'notebook'],
            save, get, clear, states = {};
        var deferred = $q.defer();
        Principal.identity()
            .then(function(user) {
                var subkey = user.id + '.autorecover.';
                var types = JSON.parse(localStorageService.get(subkey));
                if (!types) {
                    types = {}
                    kinds.forEach(function(kind) {
                        types[kind] = { recoveries: {} };
                    })
                } else {
                    //console.log('recoveries', types)
                }

                function getKey(kind, entity) {
                    return subkey + kind + '.entity.' + entity.id;
                }
                clear = function(kind, entity) {
                    //console.log('clear entity', kind, entity)
                    var type = types[kind];
                    var rec = type.recoveries[entity.id];
                    if (!rec) return;
                    delete type.recoveries[entity.id];
                    localStorageService.remove(getKey(kind, entity));
                    localStorageService.set(subkey, angular.toJson(types))
                }
                save = function(kind, entity) {
                    var clone = angular.copy(entity);
                    servfields.forEach(function(field) {
                        delete clone[field];
                    })
                    var type = types[kind];
                    var rec = type.recoveries[entity.id];
                    if (!rec) {
                        rec = type.recoveries[entity.id] = { id: entity.id, name: entity.name, kind: kind };
                    }
                    rec.date = +new Date();
                    delete rec.thisSession;
                    localStorageService.set(subkey, angular.toJson(types))
                    rec.thisSession = true;
                    localStorageService.set(getKey(kind, entity), angular.toJson(clone));
                };
                get = function(kind, entity) {
                    var type = types[kind];
                    var rec = type.recoveries[entity.id];
                    if (!rec || rec.thisSession) return;
                    return { entity: JSON.parse(localStorageService.get(getKey(kind, entity))), rec: rec }
                }
                deferred.resolve()
            });

        function canUndo(entity) {
            if (!entity) return false;
            var state = states[entity.fullId];
            return state && state.actions.length > 0 && state.aindex >= 0;
        }

        function canRedo(entity) {
            if (!entity) return false;
            var state = states[entity.fullId];
            return state && state.actions.length > 0 && state.aindex < state.actions.length - 1;
        }
        function undoAction(entity) {
            if (!entity) return false;
            var state = states[entity.fullId];
            if (state && state.aindex >= 0) {
                var act = state.actions[state.aindex--];
                console.log('undo', state.aindex, state.actions)
                angular.extend(entity, act);
                entity.$$undo = true;
            }
        }
        function redoAction(entity) {
            if (!entity) return false;
            var state = states[entity.fullId];
            if (states && state.aindex < state.actions.length - 1) {
                var act = (state.aindex < state.actions.length - 2) ? state.actions[state.aindex + 2] : state.last;
                state.aindex++;
                console.log('redo', state.aindex, state.actions)
                angular.extend(entity, act);
                entity.$$undo = true;
            }
        }
        return {
            canUndo:  canUndo,
            canRedo : canRedo,
            undoAction : undoAction,
            redoAction : redoAction,
            trackEntityChanges: function(entity, form, $scope, kind) {
                deferred.promise.then(function() {
                    var state = states[entity.fullId] || { actions: [] };
                    states[entity.fullId] = state;
                    var rec = get(kind, entity);
                    if (rec) {
                        $scope.restored = {
                            rec: rec,
                            resolve: function(val) {
                                if (val) {
                                    angular.extend(entity, rec.entity);
                                }
                                clear(kind, entity)
                                form.$setDirty(true);
                                $scope.restored = null;
                            }
                        }
                    }
                    $scope.undoAction = function() {
                        undoAction(entity)
                    }
                    $scope.redoAction = function() {
                        redoAction(entity)
                    }
                    $scope.canUndo = function() {
                        return canUndo(entity);
                    }
                    $scope.canRedo = function() {
                        return canRedo(entity);
                    }
                    var ctimeout, prev;
                    var onChange = function(entity, old) {
                        if (ctimeout) {
                            $timeout.cancel(ctimeout);
                        } else {
                            prev = angular.extend({}, old);
                        }
                        ctimeout = $timeout(function() {
                            if (form.$dirty) {
                                save(kind, entity)
                                if (!entity.$$undo) {
                                    if (state.aindex < state.actions.length - 1) {
                                        state.actions = state.actions.slice(state.aindex)
                                    }
                                    state.actions.push(prev);
                                    state.aindex = state.actions.length - 1;
                                    state.last = angular.extend({}, entity);
                                }
                            }
                            entity.$$undo = false;
                            ctimeout = null;
                        }, 1000)
                    };
                    var unbind = $scope.$watch(kind, onChange, true);
                    var unbindDirty = $scope.$watch(form.$name + '.$dirty', function(val, old) {
                        if (!val && old) {
                            clear(kind, entity)
                            $scope.restored = null;
                        }
                    }, true);
                    $scope.$on('$destroy', function() {
                        unbind();
                        unbindDirty();
                    });
                })
            },
            clearRecovery: function(kind, entity) {
                deferred.promise.then(function() {
                    clear(kind, entity)
                })
            }
        }
    });
