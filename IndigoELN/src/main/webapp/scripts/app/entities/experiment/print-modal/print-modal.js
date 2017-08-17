angular.module('indigoeln')
    .factory('printModal', function($rootScope, $stateParams, $state, $uibModal, $window, $httpParamSerializer) {
        return {
            showPopup: showPopup
        };

        function showPopup() {
            $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/experiment/print-modal/print-modal.html',
                controller: 'PrintModalController',
                controllerAs: 'vm'
            }).result.then(function(result) {
                //this won't work while response is not an object
                // entity.print(angular.extend($stateParams, result)).$promise.then(function(data) {
                //     //window.open("data:application/pdf," + escape(data)); 
                // });

                //This will go away when response of print service will be {response : 'pdfString'}
                var qs = $httpParamSerializer(result);
                var url = 'api/print/project/' + $stateParams.projectId;
                if ($stateParams.notebookId >= 0) {
                    url += '/notebook/' + $stateParams.notebookId;
                }
                if ($stateParams.experimentId >= 0) {
                    url += '/experiment/' + $stateParams.experimentId;
                }
                url += '?' + qs;
                $window.open(url);
                $state.go('^');
            }, function() {
                $state.go('^');
            });
        }
    });
