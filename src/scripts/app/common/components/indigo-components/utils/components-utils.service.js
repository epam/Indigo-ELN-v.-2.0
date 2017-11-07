angular
    .module('indigoeln.componentsModule')
    .factory('componentsUtils', componentsUtilsFactory);

/* @ngInject */
function componentsUtilsFactory(principalService, typeComponents) {
    return {
        initComponents: initComponents,
        getComponentsFromTemplateContent: getComponentsFromTemplateContent
    };

    function initComponents(components, componentTemplates) {
        var templates = _.keyBy(getComponentsFromTemplateContent(componentTemplates), 'field');

        initExperimentDescription(components, templates[typeComponents.experimentDescription.field]);
        initConceptDetails(components, templates[typeComponents.conceptDetails.field]);
        initPreferredCompoundSummary(components, templates[typeComponents.preferredCompoundSummary.field]);
        initPreferredCompoundDetails(components, templates[typeComponents.preferredCompoundDetails.field]);
        initProductBatchDetails(components, templates[typeComponents.productBatchDetails.field]);
        initProductBatchSummary(components, _.find(templates, {isBatch: true}));
        initStoichTable(components, templates[typeComponents.stoichTable.field]);
        initReactionDetails(components, templates[typeComponents.reactionDetails.field]);
        initReaction(components, templates[typeComponents.reaction.field]);
    }

    function getComponentsFromTemplateContent(componentTemplates) {
        return _.chain(componentTemplates)
            .map('components')
            .flatten()
            .value();
    }

    function initExperimentDescription(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {
                experimentDescription: {description: ''}
            });
        }
    }

    function initConceptDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(
                components, {
                    conceptDetails: {experimentCreator: principalService.getIdentity().id}
                }
            );
        }
    }

    function initPreferredCompoundSummary(components, isExist) {
        if (!isExist) {
            return;
        }
        // migration, should be removed after dropped the database
        if (!_.isEmpty(components.preferredCompoundSummary && components.preferredCompoundSummary.compounds) &&
            _.isEmpty(components.productBatchSummary && components.productBatchSummary.batches)) {
            _.set(components, 'productBatchSummary.batches', components.preferredCompoundSummary.compounds);

            delete components.preferredCompoundSummary;
        }
    }

    function initPreferredCompoundDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {preferredCompoundDetails: null});
        }
    }

    function initProductBatchDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {productBatchDetails: null});
        }
    }

    function initProductBatchSummary(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {productBatchSummary: {batches: []}});
        }
    }

    function initStoichTable(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {stoichTable: {products: [], reactants: []}});
        }
    }

    function initReactionDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(
                components, {
                    reactionDetails: {
                        batchOwner: [],
                        coAuthors: [],
                        experimentCreator: principalService.getIdentity().id
                    }
                });
        }
    }

    function initReaction(components, isExist) {
        if (isExist) {
            _.defaultsDeep(
                components, {
                    reaction: {
                        molfile: null,
                        image: null
                    }
                });
        }
    }
}
