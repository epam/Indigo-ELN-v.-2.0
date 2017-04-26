angular.module('indigoeln')
    .factory('AutoSaveEntitiesEngine', function (TabKeyUtils, AutosaveService, AlertModal, $q) {

        var delay = 5000;
        //TODO: move to service util
        var extractParams = function (obj) {
            return {
                projectId: obj.projectId,
                notebookId: obj.notebookId,
                experimentId: obj.experimentId
            };
        };

        function compactIds(params) {
            params = extractParams(params);
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
        }

        return {
            trackEntityChanges: function (entity, form, $scope, kind) {
                //replaced with autorecovery 

                // if (entity.$$form && entity.$$form.$dirty) {
                //     form.$setDirty();
                // }
                // entity.$$form = form;
                // var onChange = _.debounce(function (entity) {
                //     if (entity.$$form && entity.$$form.$dirty && entity.fullId) {
                //         AutosaveService.save({id: entity.fullId}, angular.toJson(entity));
                //     }
                // }, delay);
                // var unbind = $scope.$watch(kind, onChange, true);
                // $scope.$on('$destroy', function () {
                //     unbind();
                // });
            },


            autoRecover: function (service, params) {
                var fullId = compactIds(params);
                var deferred = $q.defer();
                var loadOrigin = function () {
                    service.get(params, function (entity) {
                        deferred.resolve(entity);
                    });
                };
                loadOrigin()
                return deferred.promise; 

                //replaced with autorecovery 

                // AutosaveService.get({id: fullId}, function (entity) {
                //     if (entity.fullId) {
                //         AlertModal.autorecover('Auto-Recover file(s) found for ' + entity.name + '. Do you want to recover from this files?', null, function () {
                //             AutosaveService.delete({id: fullId}, function () {
                //                 deferred.resolve(entity);
                //             });
                //         }, function () {
                //             AutosaveService.delete({id: fullId}, function () {
                //                 loadOrigin();
                //             });
                //         });
                //     } else {
                //         loadOrigin();
                //     }
                // });
                // return deferred.promise;
            }
        }
    });
