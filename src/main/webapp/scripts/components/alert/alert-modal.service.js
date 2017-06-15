angular
    .module('indigoeln')
    .factory('AlertModal', AlertModal);

/* @ngInject */
function AlertModal($uibModal) {

    return {
        alert: alert,
        error: error,
        warning: warning,
        info: info,
        confirm: confirm,
        save: save,
        autorecover: autorecover
    };

    function alert(title, message, size, okCallback, noCallback, okText, hideCancel, noText) {
        alertModal(title, message, size, okCallback, noCallback, okText, hideCancel, noText);
    }

    function error(msg, size) {
        alertModal('Error', msg, size);
    }

    function warning(msg, size) {
        alertModal('Warning', msg, size);
    }

    function info(msg, size, okCallback) {
        alertModal('Info', msg, size, okCallback, null);
    }

    function confirm(msg, size, okCallback) {
        alertModal('Confirm', msg, size, okCallback, null);
    }

    function save(msg, size, callback) {
        alertModal('Save', msg, size, callback.bind(null, true), callback.bind(null, false), 'Yes');
    }

    function autorecover(msg, size, okCallback, noCallback) {
        alertModal('Info', msg, size, okCallback, noCallback, 'Yes', true);
    }

    function alertModal(title, message, size, okCallback, noCallback, okText, hideCancel, noText) {
        $uibModal.open({
            size: size || 'md',
            template: '<div class="modal-header">' +
            '<h5 class="modal-title">' + title + '</h5>' +
            '</div>' +
            '<div class="modal-body long-content-popup">' +
            '<p>' + message + '</p>' +
            '</div>' +
            '<div class="modal-footer">' +
            '<button class="btn btn-primary" type="button" ng-click="ok()">{{okText}}</button>' +
            '<button class="btn btn-info" type="button" ng-click="no()" ng-if="hasNoCallback">{{noText}}</button>' +
            '<button class="btn btn-default" type="button" ng-if="cancelVisible" ng-click="cancel()"><i class="fa fa-ban"></i><span class="m-l5">Cancel</span></button>' +
            '</div>',
            controller: function ($scope, $uibModalInstance) {
                $scope.hasOkCallback = !!okCallback;
                $scope.hasNoCallback = !!noCallback;
                if (hideCancel) {
                    $scope.cancelVisible = false;
                } else {
                    $scope.cancelVisible = $scope.hasOkCallback || $scope.hasNoCallback;
                }
                $scope.okText = okText || 'OK';
                $scope.noText = noText || 'No';
                $scope.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
                $scope.ok = function () {
                    $uibModalInstance.close();
                    if ($scope.hasOkCallback) {
                        okCallback();
                    }
                };

                $scope.no = function () {
                    $uibModalInstance.close();
                    if ($scope.hasNoCallback) {
                        noCallback();
                    }
                };
            }
        });
    }
}
