package com.epam.indigoeln.core.service.print.itext2.providers;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.service.print.itext2.PdfSectionsProvider;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.*;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.BatchInformationModel.BatchInformationRow;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.BatchInformationModel.Structure;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.PreferredCompoundsModel.PreferredCompoundsRow;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.RegistrationSummaryModel.RegistrationSummaryRow;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.StoichiometryModel.ReagentInfo;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.StoichiometryModel.StoichiometryRow;
import com.epam.indigoeln.core.service.print.itext2.model.image.SvgPdfImage;
import com.epam.indigoeln.core.service.print.itext2.sections.AbstractPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.experiment.*;
import com.mongodb.BasicDBObject;
import one.util.streamex.StreamEx;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

/**
 * The class is responsible for mapping experiment to a list of pdf sections used by pdf generator.
 */
public final class ExperimentPdfSectionsProvider implements PdfSectionsProvider {
    private final Project project;
    private final Notebook notebook;
    private final Experiment experiment;

    public ExperimentPdfSectionsProvider(Project project, Notebook notebook, Experiment experiment) {
        this.project = project;
        this.notebook = notebook;
        this.experiment = experiment;
    }

    private static final HashMap<String, ComponentToPdfSectionsConverter> componentNameToConverter = new HashMap<>();

    private static final String IMAGE = "image";
    private static final String TITLE = "title";

    private static final String REACTION_DETAILS = "reactionDetails";
    private static final String CONCEPT_DETAILS = "conceptDetails";
    private static final String REACTION = "reaction";
    private static final String PREFERRED_COMPOUND_SUMMARY = "preferredCompoundSummary";
    private static final String STOICH_TABLE = "stoichTable";
    private static final String EXPERIMENT_DESCRIPTION = "experimentDescription";
    private static final String PRODUCT_BATCH_SUMMARY = "productBatchSummary";
    private static final String PRODUCT_BATCH_DETAILS = "productBatchDetails";

    private static final String TODO = "TODO";

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
                    content.joinArray("contFromRxn", "text"),
                    content.joinArray("contToRxn", "text"),
                    content.getString("codeAndName", "name"),
                    content.getString("projectAliasName"),
                    content.joinArray("linkedExperiments", "text"),
                    content.getString("reactionDetails", "literature"),
                    content.joinArray("coAuthors", "text")
            )));
        });
        put(CONCEPT_DETAILS, (c, e) -> {
            List<String> listOfTodos = Arrays.asList(TODO, TODO);
            return singletonList(new ConceptDetailsSection(new ConceptDetailsModel(
                    TODO, TODO, TODO, TODO,
                    listOfTodos, listOfTodos, listOfTodos
            )));
        });
        put(REACTION, (c, e) -> {
            String svgBase64 = c.getContent().getString(IMAGE);
            return singletonList(new ReactionSchemeSection(new ReactionSchemeModel(new SvgPdfImage(svgBase64))));
        });
        put(PREFERRED_COMPOUND_SUMMARY, (c, e) -> {
            List<PreferredCompoundsRow> rows = Arrays.asList(
                    new PreferredCompoundsRow(TODO, TODO, TODO, TODO, TODO),
                    new PreferredCompoundsRow(TODO, TODO, TODO, TODO, TODO)
            );
            return singletonList(new PreferedCompoundsSection(new PreferredCompoundsModel(rows)));
        });
        put(STOICH_TABLE, (c, e) -> {
            List<StoichiometryRow> rows = MongoExt.of(c)
                    .streamObjects("reactants")
                    .map(reactant -> new StoichiometryRow(
                            new ReagentInfo(
                                    new SvgPdfImage(reactant.getString("structure", IMAGE)),
                                    reactant.getString("fullNbkBatch"),
                                    reactant.getString("compoundId")
                            ),
                            TODO, TODO, TODO, TODO, TODO, TODO
                    ))
                    .toList();
            return singletonList(new StoichiometrySection((new StoichiometryModel(rows))));
        });
        put(EXPERIMENT_DESCRIPTION, (c, e) -> {
            ExperimentDescriptionModel model = new ExperimentDescriptionModel(c.getContent().getString("description"));
            return singletonList(new ExperimentDescriptionSection(model));
        });

        put(PRODUCT_BATCH_SUMMARY, (c, e) -> {
            List<BatchInformationRow> batchInfoRows = MongoExt.of(c)
                    .streamObjects("batches")
                    .map(batch -> {
                        BasicDBObject stereoisomer = (BasicDBObject) batch.get("stereoisomer");
                        return new BatchInformationRow(
                                batch.getString("nbkBatch"),
                                new Structure(
                                        new SvgPdfImage(batch.getString("structure", IMAGE)),
                                        stereoisomer.getString("name"),
                                        stereoisomer.getString("description")
                                ),
                                TODO, TODO, TODO, TODO
                        );
                    }).toList();

            List<RegistrationSummaryRow> regSummaryRows = MongoExt.of(c)
                    .streamObjects("batches")
                    .map(batch -> new RegistrationSummaryRow(
                            batch.getString("fullNbkBatch"),
                            TODO,
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

        put(PRODUCT_BATCH_DETAILS, (c, e) -> singletonList(new BatchDetailsSection(new BatchDetailsModel(c.getContent()))));
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
