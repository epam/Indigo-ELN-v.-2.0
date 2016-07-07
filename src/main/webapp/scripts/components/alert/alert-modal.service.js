angular.module('indigoeln')
    .factory('AlertModal', function ($uibModal) {
        var alertModal = function (title, message, size, callback) {
            $uibModal.open({
                size: size || 'md',
                template: '<div class="modal-header">' +
                '<h5 class="modal-title">' + title + '</h5>' +
                    '</div>' +
                    '<div class="modal-body">' +
                    '<p>' + message + '</p>' +
                    '</div>' +
                    '<div class="modal-footer text-right">' +
                '<button class="btn btn-info" type="button" ng-click="ok()">Ok</button>' +
                    '<button class="btn btn-default" type="button" ng-if="hasCallback" ng-click="cancel()">Cancel</button>' +
                    '</div>',
                controller: function ($scope, $uibModalInstance) {
                    $scope.hasCallback = !!callback;
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                    $scope.ok = function () {
                        $uibModalInstance.close();
                        if ($scope.hasCallback) {
                            callback();
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
            info: function (msg, size, callback) {
                alertModal('Info', msg, size, callback);
            },
            confirm: function (msg, size, callback) {
                alertModal('Confirm', msg, size, callback);
            }
        };

    });
