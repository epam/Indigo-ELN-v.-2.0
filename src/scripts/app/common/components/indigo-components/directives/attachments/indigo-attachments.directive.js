var template = require('./attachments.html');

function indigoAttachments() {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            isReadonly: '=',
            onChanged: '&'
        },
        bindToController: true,
        controllerAs: 'vm',
        template: template,
        controller: IndigoAttachmentsController
    };
}

IndigoAttachmentsController.$inject = ['apiUrl'];

function IndigoAttachmentsController(apiUrl) {
    var vm = this;
    vm.apiUrl = apiUrl;
}

module.export = indigoAttachments;

