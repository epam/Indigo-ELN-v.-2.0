angular
    .module('indigoeln')
    .factory('componentsUtils', componentsUtilsFactory);

/* @ngInject */
function componentsUtilsFactory(Principal, Components) {
    return {
        initComponents: initComponents,
        getComponentsFromTemplateContent: getComponentsFromTemplateContent
    };

    function initComponents(components, componentTemplates) {
        var templates = _.keyBy(getComponentsFromTemplateContent(componentTemplates), 'field');

        initExperimentDescription(components, templates[Components.experimentDescription.field]);
        initConceptDetails(components, templates[Components.conceptDetails.field]);
        initPreferredCompoundSummary(components, templates[Components.preferredCompoundSummary.field]);
        initPreferredCompoundDetails(components, templates[Components.preferredCompoundDetails.field]);
        initProductBatchDetails(components, templates[Components.productBatchDetails.field]);
        initProductBatchSummary(components, _.find(templates, {isBatch: true}));
        initStoichTable(components, templates[Components.stoichTable.field]);
        initReactionDetails(components, templates[Components.reactionDetails.field]);
        initReaction(components, templates[Components.reaction.field]);
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
                    conceptDetails: {experimentCreator: {name: Principal.getIdentity().fullName}}
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
                        experimentCreator: {name: Principal.getIdentity().fullName}
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
