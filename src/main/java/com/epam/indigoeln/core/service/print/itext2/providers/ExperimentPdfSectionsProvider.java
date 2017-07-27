package com.epam.indigoeln.core.service.print.itext2.providers;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.service.print.itext2.PdfSectionsProvider;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.*;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.BatchInformationModel.BatchInformation;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.BatchInformationModel.BatchInformationRow;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.PreferredCompoundsModel.PreferredCompoundsRow;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.RegistrationSummaryModel.RegistrationSummaryRow;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.StoichiometryModel.StoichiometryRow;
import com.epam.indigoeln.core.service.print.itext2.model.image.SvgPdfImage;
import com.epam.indigoeln.core.service.print.itext2.sections.AbstractPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.experiment.*;
import com.epam.indigoeln.core.service.print.itext2.utils.LogoUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.MongoExt;
import one.util.streamex.StreamEx;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static com.epam.indigoeln.core.service.print.itext2.model.experiment.PreferredCompoundsModel.*;
import static java.util.Collections.singletonList;

/**
 * The class is responsible for mapping experiment to a list of pdf sections used by pdf generator.
 */
public final class ExperimentPdfSectionsProvider implements PdfSectionsProvider {
    private final Project project;
    private final Notebook notebook;
    private final Experiment experiment;

    private static final HashMap<String, ComponentToPdfSectionsConverter> componentNameToConverter = new HashMap<>();

    private static final String TITLE = "title";

    private static final String REACTION_DETAILS = "reactionDetails";
    private static final String CONCEPT_DETAILS = "conceptDetails";
    private static final String REACTION = "reaction";
    private static final String PREFERRED_COMPOUND_SUMMARY = "preferredCompoundSummary";
    private static final String STOICH_TABLE = "stoichTable";
    private static final String EXPERIMENT_DESCRIPTION = "experimentDescription";
    private static final String PRODUCT_BATCH_SUMMARY = "productBatchSummary";
    private static final String PRODUCT_BATCH_DETAILS = "productBatchDetails";

    public ExperimentPdfSectionsProvider(Project project, Notebook notebook, Experiment experiment) {
        this.project = project;
        this.notebook = notebook;
        this.experiment = experiment;
    }

    /**
     * @return list of raw uninitialized pdf sections corresponding to experiment components.
     */
    public List<AbstractPdfSection> getContentSections() {
        return StreamEx
                .of(experiment.getComponents())
                .flatMap(this::sections)
                .toList();
    }

    private Stream<AbstractPdfSection> sections(Component component) {
        return Optional.ofNullable(componentNameToConverter.get(component.getName()))
                .map(converter -> converter.convert(component, experiment).stream())
                .orElseGet(Stream::empty);
    }

    static {
        put(REACTION_DETAILS, (c, e) -> {
            MongoExt content = MongoExt.of(c);
            return singletonList(new ReactionDetailsSection(new ReactionDetailsModel(
                    e.getCreationDate(),
                    content.getString("therapeuticArea", "name"),
                    content.streamObjects("contFromRxn").map(m -> m.getString("text")).toList(),
                    content.streamObjects("contToRxn").map(m -> m.getString("text")).toList(),
                    content.getString("codeAndName", "name"),
                    content.getString("projectAliasName"),
                    content.streamObjects("linkedExperiments").map(m -> m.getString("text")).toList(),
                    content.getString("literature"),
                    content.streamObjects("coAuthors").map(m -> m.getString("name")).toList()
            )));
        });
        put(CONCEPT_DETAILS, (c, e) -> {
            MongoExt content = MongoExt.of(c);
            return singletonList(new ConceptDetailsSection(new ConceptDetailsModel(
                    e.getCreationDate(),
                    content.getString("therapeuticArea", "name"),
                    content.streamObjects("linkedExperiments").map(m -> m.getString("text")).toList(),
                    content.getString("codeAndName", "name"),
                    content.getString("keywords"),
                    content.streamObjects("designers").map(m -> m.getString("name")).toList(),
                    content.streamObjects("coAuthors").map(m -> m.getString("name")).toList()
            )));
        });
        put(REACTION, (c, e) -> {
            MongoExt content = MongoExt.of(c);
            String svgBase64 = content.getString("image");
            return singletonList(new ReactionSchemeSection(new ReactionSchemeModel(new SvgPdfImage(svgBase64))));
        });
        put(PREFERRED_COMPOUND_SUMMARY, (c, e) -> {
            MongoExt content = MongoExt.of(c);
            List<PreferredCompoundsRow> rows = content.streamObjects("compounds")
                    .map(compound -> {
                        MongoExt stereoisomerObj = compound.getObject("stereoisomer");
                        Structure structure = new Structure(compound.getString("virtualCompoundId"),
                                stereoisomerObj.getString("name"),
                                stereoisomerObj.getString("description"));
                        return new PreferredCompoundsRow(
                                structure,
                                compound.getString("fullNbkBatch"),
                                compound.getString("molWeight", "value"),
                                compound.getString("formula"),
                                compound.getString("structureComments")
                        );
                    }).toList();
            return singletonList(new PreferedCompoundsSection(new PreferredCompoundsModel(rows)));
        });
        put(STOICH_TABLE, (c, e) -> {
            MongoExt content = MongoExt.of(c);
            List<StoichiometryRow> rows = content.streamObjects("reactants")
                    .map(reactant -> new StoichiometryRow(
                            reactant.getString("fullNbkBatch"),
                            reactant.getString("compoundId"),
                            new StoichiometryModel.Structure(new SvgPdfImage(reactant.getString("structure", "image"))),
                            reactant.getString("molWeight", "value"),
                            reactant.getString("weight", "value"),
                            reactant.getString("weight", "unit"),
                            reactant.getString("mol", "value"),
                            reactant.getString("mol", "unit"),
                            reactant.getString("volume", "value"),
                            reactant.getString("volume", "unit"),
                            reactant.getString("eq", "value"),
                            reactant.getString("chemicalName"),
                            reactant.getString("rxnRole", "name"),
                            reactant.getString("stoicPurity", "value"),
                            reactant.getString("molarity", "value"),
                            reactant.getString("molarity", "unit"),
                            reactant.getString("hazardComments"),
                            reactant.getString("saltCode", "name"),
                            reactant.getString("saltEq", "value"),
                            reactant.getString("comments"))).toList();
            return singletonList(new StoichiometrySection((new StoichiometryModel(rows))));
        });
        put(EXPERIMENT_DESCRIPTION, (c, e) -> {
            MongoExt content = MongoExt.of(c);
            ExperimentDescriptionModel model = new ExperimentDescriptionModel(content.getString("description"));
            return singletonList(new ExperimentDescriptionSection(model));
        });

        put(PRODUCT_BATCH_SUMMARY, (c, e) -> {
            Optional<List<String>> batchOwner = e.getComponents().stream()
                    .filter(component -> REACTION_DETAILS.equals(component.getName()))
                    .map(MongoExt::of)
                    .map(m -> m.streamObjects("batchOwner").map(owner -> owner.getString("name")).toList())
                    .findAny();

            List<BatchInformationRow> batchInfoRows = MongoExt.of(c)
                    .streamObjects("batches")
                    .map(batch -> {
                        MongoExt stereoisomer = batch.getObject("stereoisomer");
                        return new BatchInformationRow(
                                batch.getString("nbkBatch"),
                                new BatchInformationModel.Structure(
                                        new SvgPdfImage(batch.getString("structure", "image")),
                                        stereoisomer.getString("name"),
                                        stereoisomer.getString("description")
                                ),
                                batch.getString("totalWeight", "value"),
                                batch.getString("totalWeight", "unit"),
                                batch.getString("theoWeight", "value"),
                                batch.getString("theoWeight", "unit"),
                                batch.getString("yield"),
                                batch.getString("stoicPurity", "value"),
                                new BatchInformation(
                                        batch.getString("molWeight", "value"),
                                        batch.getString("exactMass"),
                                        batch.getString("saltCode", "name"),
                                        batch.getString("saltEq", "value"),
                                        batchOwner.orElse(Collections.emptyList()),
                                        batch.getString("comments")
                                )
                        );
                    }).toList();

            List<RegistrationSummaryRow> regSummaryRows = MongoExt.of(c)
                    .streamObjects("batches")
                    .map(batch -> new RegistrationSummaryRow(
                            batch.getString("fullNbkBatch"),
                            batch.getString("totalWeight", "value"),
                            batch.getString("totalWeight", "unit"),
                            batch.getString("registrationStatus"),
                            batch.getString("conversationalBatchNumber")
                    ))
                    .filter(row -> Objects.equals(row.getRegistrationStatus(), "PASSED"))
                    .toList();

            return Arrays.asList(
                    new BatchInformationSection(new BatchInformationModel(batchInfoRows)),
                    new RegistrationSummarySection(new RegistrationSummaryModel(regSummaryRows))
            );
        });

        put(PRODUCT_BATCH_DETAILS, (c, e) -> {
            Optional<MongoExt> content = e.getComponents().stream()
                    .filter(component -> PRODUCT_BATCH_SUMMARY.equals(component.getName()))
                    .map(Component::getContent)
                    .map(MongoExt::of)
                    .findAny();

            Optional<List<String>> batchOwner = e.getComponents().stream()
                    .filter(component -> REACTION_DETAILS.equals(component.getName()))
                    .map(MongoExt::of)
                    .map(m -> m.streamObjects("batchOwner").map(owner -> owner.getString("name")).toList())
                    .findAny();

            Optional<List<AbstractPdfSection>> sections = content.map(m -> m.streamObjects("batches")
                    .map(batch -> (AbstractPdfSection) new BatchDetailsSection(new BatchDetailsModel(
                            batch.getString("fullNbkBatch"),
                            batch.getString("registrationDate"),
                            batch.getString("structureComments"),
                            batch.getString("source", "name"),
                            batch.getString("sourceDetail", "name"),
                            batchOwner.orElse(Collections.emptyList()),
                            batch.getString("molWeight", "value"),
                            batch.getString("formula"),
                            "TODO",
                            batch.getString("solubility", "asString"),
                            batch.getString("precursors"),
                            batch.getString("externalSupplier", "asString"),
                            batch.getString("healthHazards", "asString"),
                            batch.getString("handlingPrecautions", "asString"),
                            batch.getString("storageInstructions", "asString")
                    )))
                    .toList());

            return sections.orElse(Collections.emptyList());
        });
    }

    private static void put(String name, ComponentToPdfSectionsConverter builder) {
        componentNameToConverter.put(name, builder);
    }

    public ExperimentHeaderSection getHeaderSection() {
        List<Component> components = experiment.getComponents();
        Optional<String> title1 = StreamEx.of(components)
                .findFirst(c -> c.getName().equals(REACTION_DETAILS))
                .map(Component::getContent).map(c -> c.getString(TITLE));
        Optional<String> title2 = StreamEx.of(components)
                .findFirst(c -> c.getName().equals(CONCEPT_DETAILS))
                .map(Component::getContent).map(c -> c.getString(TITLE));

        return new ExperimentHeaderSection(new ExperimentHeaderModel(
                LogoUtils.loadDefaultLogo(),
                Instant.now(),
                experiment.getAuthor().getFullName(),
                notebook.getName() + "-" + experiment.getName(),
                project.getName(),
                experiment.getStatus().toString(),
                title1.orElse(title2.orElse(""))
        ));
    }

    /**
     * All inheritors are responsible for building a list of pdf sections
     * (one component can correspond to several pdf sections)
     */
    @FunctionalInterface
    private interface ComponentToPdfSectionsConverter {
        List<AbstractPdfSection> convert(Component component, Experiment e);
    }

    private static class PdfSectionsBuilderException extends RuntimeException {
        PdfSectionsBuilderException(Throwable e) {
            super(e);
        }
    }
}
