(function() {
    angular
        .module('indigoeln')
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
            templateUrl: 'scripts/components/entities/template/components/attachments/attachments.html',
            controller: function() {

            }
        };
    }
})();
