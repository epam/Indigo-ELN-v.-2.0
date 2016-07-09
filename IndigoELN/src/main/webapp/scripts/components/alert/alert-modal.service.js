angular.module('indigoeln')
    .factory('AlertModal', function ($uibModal) {
        var alertModal = function (title, message, size, okCallback, noCallback, okText) {
            $uibModal.open({
                size: size || 'md',
                template: '<div class="modal-header">' +
                '<h5 class="modal-title">' + title + '</h5>' +
                    '</div>' +
                    '<div class="modal-body">' +
                    '<p>' + message + '</p>' +
                    '</div>' +
                    '<div class="modal-footer text-right">' +
                '<button class="btn btn-info" type="button" ng-click="ok()">{{okText}}</button>' +
                '<button class="btn btn-info" type="button" ng-click="no()" ng-if="hasNoCallback">No</button>' +
                '<button class="btn btn-default" type="button" ng-if="hasOkCallback || hasNoCallback" ng-click="cancel()">Cancel</button>' +
                    '</div>',
                controller: function ($scope, $uibModalInstance) {
                    $scope.hasOkCallback = !!okCallback;
                    $scope.hasNoCallback = !!noCallback;
                    $scope.okText = okText || 'Ok';
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
        };
        return {
            error: function (msg, size) {
                alertModal('Error', msg, size);
            },
            warning: function (msg, size) {
                alertModal('Warning', msg, size);
            },
            info: function (msg, size, okCallback) {
                alertModal('Info', msg, size, okCallback, null);
            },
            confirm: function (msg, size, okCallback) {
                alertModal('Confirm', msg, size, okCallback, null);
            },
            save: function (msg, size, callback) {
                alertModal('Save', msg, size, callback.bind(null, true), callback.bind(null, false), 'Yes');
            }
        };

    });
