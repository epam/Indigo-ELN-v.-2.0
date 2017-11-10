var template = require('./print-modal.html');

printModal.$inject = ['$uibModal', '$window', '$httpParamSerializer', 'apiUrl'];

function printModal($uibModal, $window, $httpParamSerializer, apiUrl) {
    return {
        showPopup: showPopup
    };

    function showPopup(params, resourceName) {
        $uibModal.open({
            animation: true,
            template: template,
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
            var url = apiUrl + 'print/project/' + params.projectId;
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
}

module.exports = printModal;
