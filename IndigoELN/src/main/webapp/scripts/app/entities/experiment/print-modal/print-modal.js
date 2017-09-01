angular.module('indigoeln')
    .factory('printModal', function($rootScope, $state, $uibModal, $window, $httpParamSerializer) {
        return {
            showPopup: showPopup
        };

        function showPopup(params, resourceName) {
            $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/experiment/print-modal/print-modal.html',
                controller: 'PrintModalController',
                controllerAs: 'vm',
                resolve: {
                    params: params,
                    resource: [resourceName, function(resource) {
                        return resource;
                    }],
                    resourceName: function() {
                        return resourceName;
                    }
                }
            }).result.then(function(result) {
                var qs = $httpParamSerializer(result);
                var url = 'api/print/project/' + params.projectId;
                if (params.notebookId) {
                    url += '/notebook/' + params.notebookId;
                }
                if (params.experimentId) {
                    url += '/experiment/' + params.experimentId;
                }
                url += '?' + qs;
                $window.open(url);
            });
        }
    });
