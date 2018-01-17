var template = require('./confirmation-modal.html');

/* @ngInject */
function confirmationModal($uibModal, $q) {
    return {
        open: open,
        openEntityVersionsConflictConfirm: openEntityVersionsConflictConfirm
    };

    function open(resolve, size) {
        return $uibModal.open({
            bindToController: true,
            controllerAs: 'vm',
            resolve: resolve,
            size: size || 'md',
            template: template,
            controller: 'ConfirmationModalController'
        });
    }

    function generateWarningMessage(entityName) {
        return entityName +
            ' has been changed by another user while you have not applied changes. ' +
            'You can Accept or Reject saved changes. ' +
            '"Accept" button reloads page to show saved data,' +
            ' "Reject" button leave entered data and allows you to save them.';
    }

    function openEntityVersionsConflictConfirm(entityName) {
        return open({
            title: function() {
                return 'Warning';
            },
            message: function() {
                return generateWarningMessage(entityName);
            },
            buttons: function() {
                return {
                    yes: 'Accept',
                    no: 'Reject'
                };
            }
        }).result.then(function(status) {
            return status === 'resolve' ? $q.resolve() : $q.reject();
        });
    }
}

module.exports = confirmationModal;