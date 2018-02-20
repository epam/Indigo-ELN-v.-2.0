var template = require('./print-modal.html');

/* @ngInject */
function printModal($uibModal, $window, $httpParamSerializer, apiUrl) {
    var dlg;

    return {
        showPopup: showPopup,
        close: close
    };

    function showPopup(params, resourceType) {
        dlg = $uibModal.open({
            animation: true,
            template: template,
            controller: 'PrintModalController',
            controllerAs: 'vm',
            resolve: {
                params: params,
                resourceType: function() {
                    return resourceType;
                }
            }
        });

        return dlg.result.then(function(result) {
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

    function close() {
        if (dlg) {
            dlg.dismiss();
            dlg = null;
        }
    }
}

module.exports = printModal;
