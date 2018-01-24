/* @ngInject */
function entityHelper(CacheFactory, tabKeyService, confirmationModal, notifyService, entityTreeService) {
    var versionCache = CacheFactory.get('versionCache') || CacheFactory('versionCache');
    var isConflictConfirmOpen = false;

    return {
        checkVersion: checkVersion
    };

    function checkVersion($stateParams, data, currentEntity, entityTitle, isChanged, refreshClbk) {
        var changedParentEntity = getChangedParentEntity(currentEntity.parentId, $stateParams, data.entity);
        var hasChangeInTheEntity = _.isEqual($stateParams, data.entity) && data.version > currentEntity.version;

        // If nothing changed in current entity or its parents do nothing
        if (!changedParentEntity && !hasChangeInTheEntity) {
            return;
        }

        versionCache.put(tabKeyService.getTabKeyFromParams($stateParams), data.version);

        if (changedParentEntity) {
            var parentEntity;

            if (changedParentEntity.notebookId) {
                parentEntity = entityTreeService.getNotebookById(changedParentEntity.notebookId);
            } else {
                parentEntity = entityTreeService.getProjectById(changedParentEntity.projectId);
            }

            changedParentEntity.name = _.get(parentEntity, 'name', null);
        }

        // Has local changes which could be saved before update
        if (isChanged) {
            if (isConflictConfirmOpen) {
                return;
            }

            isConflictConfirmOpen = confirmationModal
                .openEntityVersionsConflictConfirm(entityTitle)
                .then(refreshClbk, function() {
                    currentEntity.version = versionCache.get(tabKeyService.getTabKeyFromParams($stateParams));
                })
                .finally(function() {
                    isConflictConfirmOpen = false;
                });

            return;
        }

        refreshClbk().then(function() {
            var message = entityTitle + ' has been changed by another user and had been reloaded';
            var currentEntityType = $stateParams.experimentId ? 'experiment' : 'notebook';

            if (changedParentEntity) {
                message = changedParentEntity.type + ' ' + changedParentEntity.name +
                    ' has been changed by another user and current ' + currentEntityType + ' ' +
                    entityTitle + ' had been reloaded';
            }

            notifyService.info(message);
        });
    }

    function getChangedParentEntity(parentId, currentEntity, changedEntity) {
        if (!parentId || _.isEqual(currentEntity, changedEntity)) {
            return null;
        }

        var parentNotebook = currentEntity.notebookId;
        var parentProject = currentEntity.projectId;

        if (parentNotebook) {
            var parentNotebookEntity = {
                notebookId: parentNotebook,
                projectId: parentProject
            };

            if (_.isEqual(changedEntity, parentNotebookEntity)) {
                parentNotebookEntity.type = 'Notebook';

                return parentNotebookEntity;
            }
        }

        var parentProjectEntity = {
            projectId: parentProject
        };

        if (_.isEqual(changedEntity, parentProjectEntity)) {
            parentProjectEntity.type = 'Project';

            return parentProjectEntity;
        }

        return null;
    }
}

module.exports = entityHelper;
