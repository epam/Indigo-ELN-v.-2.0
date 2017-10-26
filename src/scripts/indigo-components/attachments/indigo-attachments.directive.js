(function() {
    angular
        .module('indigoeln.Components')
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
            templateUrl: 'scripts/indigo-components/attachments/attachments.html',
            controller: function() {

            }
        };
    }
})();
