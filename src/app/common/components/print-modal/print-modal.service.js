var template = require('./print-modal.html');

printModal.$inject = ['$uibModal', '$window', '$httpParamSerializer', 'apiUrl'];

function printModal($uibModal, $window, $httpParamSerializer, apiUrl) {
    return {
        showPopup: showPopup,
        close: close
    };

    var dlg;

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
        }
    }
}

module.exports = printModal;
