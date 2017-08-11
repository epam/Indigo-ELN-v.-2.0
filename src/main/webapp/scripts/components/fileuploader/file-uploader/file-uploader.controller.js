(function() {
    angular
        .module('indigoeln')
        .controller('FileUploaderController', FileUploaderController);

    function FileUploaderController($uibModal, $filter, $stateParams, FileUploaderCash,
                                    ParseLinks, notifyService, ProjectFileUploaderService, ExperimentFileUploaderService) {
        var vm = this;
        var params = $stateParams;
        var UploaderService = params.experimentId ? ExperimentFileUploaderService : ProjectFileUploaderService;

        init();

        function init() {
            vm.page = 1;
            vm.loadAll = loadAll;
            vm.loadPage = loadPage;
            vm.upload = upload;
            vm.deleteFile = deleteFile;
            vm.search = search;


            if (params.projectId) {
                vm.loadAll();
            }
        }

        function loadAll() {
            UploaderService.query({
                projectId: params.projectId,
                experimentId: params.projectId + '-' + params.notebookId + '-' + params.experimentId,
                page: vm.page - 1,
                size: 20
            }, function(result, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.files = result;
            });
        }

        function loadPage(page) {
            vm.page = page;
            vm.loadAll();
        }

        function upload() {
            if (!params.projectId) {
                notifyService.error('Please save project before attach files.');

                return;
            }

            $uibModal.open({
                animation: true,
                size: 'lg',
                templateUrl: 'scripts/components/fileuploader/multiple-file-uploader/multiple-file-uploader.html',
                controller: 'MultipleFileUploaderController',
                controllerAs: 'vm',
                resolve: {
                    params: function() {
                        return params;
                    },
                    uploadUrl: function() {
                        return vm.uploadUrl;
                    }
                }
            }).result.then(function(result) {
                vm.files = _.union(vm.files, result);
                if (vm.files.length) {
                    vm.onChanged({files: vm.files});
                }
            });
        }

        function deleteFile(file) {
            $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/fileuploader/delete-dialog/delete-dialog.html',
                controller: 'FileUploaderDeleteDialogController',
                controllerAs: 'vm',
                resolve: {
                    params: function() {
                        return params;
                    },
                    file: function() {
                        return file;
                    },
                    UploaderService: function() {
                        return UploaderService;
                    }
                }
            }).result.then(function(file) {
                vm.files = _.without(vm.files, file);
                FileUploaderCash.removeFile(file);
                notifyService.success('File was successfully deleted');
            });
        }

        function search() {
            UploaderService.query({
                projectId: params.projectId,
                experimentId: params.projectId + '-' + params.notebookId + '-' + params.experimentId,
                page: vm.page - 1,
                size: 5
            }, function(result, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.files = $filter('filter')(result, vm.searchText);
            });
        }
    }
})();
