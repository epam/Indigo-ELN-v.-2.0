(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('indigoAttachments', indigoAttachments);

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
            templateUrl: 'scripts/app/common/components/indigo-components/attachments/attachments.html',
            controller: IndigoAttachmentsController
        };
    }

    IndigoAttachmentsController.$inject = ['apiUrl'];

    function IndigoAttachmentsController(apiUrl) {
        var vm = this;
        vm.apiUrl = apiUrl;
    }
})();
