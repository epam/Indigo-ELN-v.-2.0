'use strict';

angular.module('indigoeln')
    .factory('AlertModal', function ($uibModal) {
        var alertModal = function (title, message, callback) {
            $uibModal.open({
                template: '<div class="modal-header">' +
                    '<h4 class="modal-title">' + title + '</h4>' +
                    '</div>' +
                    '<div class="modal-body">' +
                    '<p>' + message + '</p>' +
                    '</div>' +
                    '<div class="modal-footer text-right">' +
                    '<button class="btn btn-primary" type="button" ng-if="!hasCallback" ng-click="cancel()">Ok</button>' +
                    '<button class="btn btn-primary" type="button" ng-if="hasCallback" ng-click="yes()">Yes</button>' +
                    '<button class="btn btn-default" type="button" ng-if="hasCallback" ng-click="cancel()">No</button>' +
                    '</div>',
                controller: function ($scope, $uibModalInstance) {
                    $scope.hasCallback = !!callback;
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                    $scope.yes = function () {
                        $uibModalInstance.close();
                        if ($scope.hasCallback) {
                            callback();
                        }
                    };
                }
            });
        };
        return {
            error: function (msg) {
                alertModal('Error', msg);
            },
            warning: function (msg) {
                alertModal('Warning', msg);
            },
            info: function (msg) {
                alertModal('Info', msg);
            },
            confirm: function (msg, callback) {
                alertModal('Confirm', msg, callback);
            }
        };

    });
