var multipleFileUploaderTemplate = require('../multiple-file-uploader/multiple-file-uploader.html');
var deleteDialogTemplate = require('../delete-dialog/delete-dialog.html');

FileUploaderController.$inject = ['$uibModal', '$filter', '$stateParams', 'fileUploaderService',
    'parseLinksService', 'notifyService', 'projectFileUploaderService',
    'experimentFileUploaderService', '$timeout', 'apiUrl'];

function FileUploaderController($uibModal, $filter, $stateParams, fileUploaderService,
                                parseLinksService, notifyService, projectFileUploaderService,
                                experimentFileUploaderService, $timeout, apiUrl) {
    var vm = this;
    var params = $stateParams;
    var UploaderService = params.experimentId ? experimentFileUploaderService : projectFileUploaderService;

    init();

    function init() {
        vm.apiUrl = apiUrl;
        vm.pagination = {
            page: 1,
            pageSize: 10
        };

        vm.loadAll = loadAll;
        vm.upload = upload;
        vm.deleteFile = deleteFile;
        vm.search = _.debounce(search, 300);
        vm.onPageChanged = onPageChanged;

        if (params.projectId) {
            vm.loadAll();
        }
    }

    function loadAll() {
        UploaderService.query({
            projectId: params.projectId,
            notebookId: params.notebookId,
            experimentId: params.experimentId
        }, function(result, headers) {
            vm.links = parseLinksService.parse(headers('link'));
            vm.files = result;
            updateRowsForDisplay(vm.files);
        });
    }

    function upload() {
        if (!params.projectId) {
            notifyService.error('Please save project before attach files.');

            return;
        }

        $uibModal.open({
            animation: true,
            size: 'lg',
            template: multipleFileUploaderTemplate,
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
            vm.files = _.union(result, vm.files);
            updateRowsForDisplay(vm.files);
            if (vm.files.length) {
                vm.onChanged({files: vm.files});
            }
        });
    }

    function deleteFile(file) {
        $uibModal.open({
            animation: true,
            template: deleteDialogTemplate,
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
        }).result.then(function(fileToDelete) {
            vm.files = _.without(vm.files, fileToDelete);
            updateRowsForDisplay(vm.files);
            fileUploaderService.removeFile(fileToDelete);
            notifyService.success('File was successfully deleted');
        });
    }

    function search() {
        vm.filteredFiles = $filter('filter')(vm.files, vm.searchText);
    }

    function onPageChanged() {
        updateRowsForDisplay(vm.rowsForDisplay);
    }

    function getSkipItems() {
        return (vm.pagination.page - 1) * vm.pagination.pageSize;
    }

    function updateRowsForDisplay(rows) {
        if (!rows || rows.length === 0) {
            vm.limit = 0;
            vm.rowsForDisplay = null;

            return;
        }
        var skip = getSkipItems(rows);
        if (skip >= rows.length) {
            updateCurrentPage(rows);
            skip = getSkipItems();
        }
        $timeout(function() {
            vm.limit = skip + vm.pagination.pageSize;
            vm.rowsForDisplay = rows;
        });
    }

    function updateCurrentPage(rows) {
        vm.pagination.page = _.ceil(rows.length / vm.pagination.pageSize);
    }
}

module.exports = FileUploaderController;
