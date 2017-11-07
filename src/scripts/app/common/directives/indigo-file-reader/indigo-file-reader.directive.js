function indigoFileReader() {
    IndigoFileReaderController.$inject = ['$element'];

    return {
        restrict: 'A',
        scope: {
            indigoFileReader: '&'
        },
        controller: IndigoFileReaderController,
        controllerAs: 'vm',
        bindToController: true
    };

    function IndigoFileReaderController($element) {
        var vm = this;

        $onInit();

        function $onInit() {
            $element.on('change', onChangeElement);
        }

        function loadFile(blob) {
            var reader = new FileReader();
            reader.onload = function(onLoadEvent) {
                vm.indigoFileReader({fileContent: onLoadEvent.target.result});
            };

            reader.readAsText(blob);
        }

        function onChangeElement(onChangeEvent) {
            var blob = (onChangeEvent.srcElement || onChangeEvent.target).files[0];
            if (!blob) {
                vm.indigoFileReader({fileContent: null});

                return;
            }

            loadFile(blob);
        }
    }
}

module.exports = indigoFileReader;
