'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('tab', {
                abstract: true,
                parent: 'entity',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/tabs/tab-manager.html',
                        controller: 'TabManagerController'
                    }
                },
                resolve: {
                    data: function ($state, $stateParams, EntitiesBrowser, TabManager, $q) {
                        var deferred = $q.defer();
                        //EntitiesBrowser
                        //    .resolveTabs($stateParams)
                        //    .then(function (tabs) {
                        //        //var kind = EntitiesBrowser.getKind($stateParams);
                        //        //if (kind === 'experiment') {
                        //        //    EntitiesBrowser.resolveFromCache({
                        //        //        projectId: $stateParams.projectId,
                        //        //        notebookId: $stateParams.notebookId
                        //        //    }).then(function () {
                        //        //        deferred.resolve({
                        //        //            entities: tabs
                        //        //        });
                        //        //    });
                        //        //} else {
                        //        //    deferred.resolve({
                        //        //        entities: tabs
                        //        //    });
                        //        //}
                        //    });
                        //return deferred.promise;
                    }
                }
            });
    });
