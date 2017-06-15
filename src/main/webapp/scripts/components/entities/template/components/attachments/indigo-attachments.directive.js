(function () {
    angular
        .module('indigoeln')
        .directive('indigoAttachments', indigoAttachments);

    function indigoAttachments() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/attachments/attachments.html'
        };
    }
})();