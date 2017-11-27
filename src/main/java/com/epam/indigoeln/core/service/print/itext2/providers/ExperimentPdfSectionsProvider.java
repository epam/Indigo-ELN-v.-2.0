package com.epam.indigoeln.core.service.print.itext2.providers;

import com.epam.indigoeln.core.model.*;
import com.epam.indigoeln.core.repository.file.FileRepository;
import com.epam.indigoeln.core.repository.user.UserRepository;
import com.epam.indigoeln.core.service.print.itext2.PdfSectionsProvider;
import com.epam.indigoeln.core.service.print.itext2.model.common.AttachmentsModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.DescriptionModel;
import com.epam.indigoeln.core.service.print.itext2.model.common.image.SvgPdfImage;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.*;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.BatchInformationModel.BatchInformation;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.BatchInformationModel.BatchInformationRow;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.PreferredCompoundsModel.PreferredCompoundsRow;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.RegistrationSummaryModel.RegistrationSummaryRow;
import com.epam.indigoeln.core.service.print.itext2.model.experiment.StoichiometryModel.StoichiometryRow;
import com.epam.indigoeln.core.service.print.itext2.sections.common.AbstractPdfSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.AttachmentsSection;
import com.epam.indigoeln.core.service.print.itext2.sections.common.DescriptionSection;
import com.epam.indigoeln.core.service.print.itext2.sections.experiment.*;
import com.epam.indigoeln.core.service.print.itext2.utils.LogoUtils;
import com.epam.indigoeln.core.service.print.itext2.utils.MongoExt;
import com.epam.indigoeln.web.rest.dto.FileDTO;
import com.epam.indigoeln.web.rest.dto.print.PrintRequest;
import com.mongodb.gridfs.GridFSDBFile;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.PageRequest;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static com.epam.indigoeln.core.service.print.itext2.model.experiment.PreferredCompoundsModel.Structure;
import static com.epam.indigoeln.core.util.BatchComponentUtil.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * The class is responsible for mapping experiment to a list of pdf sections used by pdf generator.
 */
public final class ExperimentPdfSectionsProvider implements PdfSectionsProvider {
    private final Project project;
    private final Notebook notebook;
    private final Experiment experiment;
    private final FileRepository fileRepository;
    private final List<GridFSDBFile> files;
    private final PrintRequest printRequest;
    private final UserRepository userRepository;

    private static final HashMap<String, ComponentToPdfSectionsConverter> componentNameToConverter = new HashMap<>();

    static {
        put(REACTION, ExperimentPdfSectionsProvider::reactionConverter);
        put(PREFERRED_COMPOUND_SUMMARY, ExperimentPdfSectionsProvider::preferredCompoundSummaryConverter);
        put(STOICH_TABLE, ExperimentPdfSectionsProvider::stoichTableConverter);
        put(EXPERIMENT_DESCRIPTION, ExperimentPdfSectionsProvider::experimentDescriptionConverter);
    }

    private final String[] THERAPEUTIC_AREA_NAME = {"therapeuticArea", "name"};
    private final String[] CODE_AND_NAME_NAME = {"codeAndName", "name"};
    private final String[] CONTINUED_FROM_NAME = {"contFromRxn", "name"};
    private final String[] CONTINUED_TO_NAME = {"contToRxn", "name"};

    public ExperimentPdfSectionsProvider(Project project, Notebook notebook, Experiment experiment, FileRepository fileRepository,
                                         PrintRequest printRequest, UserRepository userRepository) {
        this.project = project;
        this.notebook = notebook;
        this.experiment = experiment;
        this.fileRepository = fileRepository;
        this.files = getFiles(experiment.getFileIds());
        this.printRequest = printRequest;
        this.userRepository = userRepository;
        put(REACTION_DETAILS, this::reactionDetailsConverter);
        put(CONCEPT_DETAILS, this::conceptDetailsConverter);
        put(PRODUCT_BATCH_SUMMARY, this::productBatchSummaryConverter);
        put(PRODUCT_BATCH_DETAILS, this::productBatchDetailsConverter);
    }

    /**
     * @return list of raw uninitialized pdf sections corresponding to experiment components.
     */
    public List<AbstractPdfSection> getContentSections() {
        List<String> components = printRequest.getComponents();

        List<AbstractPdfSection> contentSections = components
                .stream()
                .flatMap(this::sectionOfComponent)
                .collect(toList());
        if (components.contains(ATTACHMENTS)) {
            contentSections.add(getAttachmentsSection());
        }
        return contentSections;
    }

    private Stream<AbstractPdfSection> sectionOfComponent(String printedComponentName) {
        Optional<Component> componentOfExperiment = getComponentOfExperiment(printedComponentName);
        if (componentOfExperiment.isPresent()) {
            return Optional.ofNullable(componentNameToConverter.get(printedComponentName))
                    .map(converter -> converter.convert(Pair.of(componentOfExperiment.get(), experiment)).stream())
                    .orElse(Stream.empty());
        }
        return Stream.empty();
    }

    private Optional<Component> getComponentOfExperiment(String printedComponentName) {
        return experiment.getComponents().stream()
                .filter(component -> (PREFERRED_COMPOUND_SUMMARY.equals(printedComponentName) && PRODUCT_BATCH_SUMMARY.equals(component.getName()))
                        || printedComponentName.equals(component.getName()))
                .findAny();
    }

    private static void put(String name, ComponentToPdfSectionsConverter builder) {
        componentNameToConverter.put(name, builder);
    }

    private List<AbstractPdfSection> reactionDetailsConverter(Pair<Component, Experiment> p) {
        return MongoExt.of(p.getLeft()).map(content -> singletonList(new ReactionDetailsSection(new ReactionDetailsModel(
                p.getRight().getCreationDate(),
                content.getString(THERAPEUTIC_AREA_NAME),
                content.getString(CONTINUED_FROM_NAME),
                content.getString(CONTINUED_TO_NAME),
                content.getString(CODE_AND_NAME_NAME),
                content.getString("projectAliasName"),
                content.streamObjects("linkedExperiments").map(m -> m.getString("text")).toList(),
                content.getString("literature"),
                userRepository.findAll(content.streamStrings("coAuthors").toList()).stream().map(User::getFullName).collect(toList()))
        )));
    }

    private List<AbstractPdfSection> conceptDetailsConverter(Pair<Component, Experiment> p) {
        return MongoExt.of(p.getLeft()).map(content -> singletonList(new ConceptDetailsSection(new ConceptDetailsModel(
                p.getRight().getCreationDate(),
                content.getString(THERAPEUTIC_AREA_NAME),
                content.streamObjects("linkedExperiments").map(m -> m.getString("text")).toList(),
                content.getString(CODE_AND_NAME_NAME),
                content.getString("keywords"),
                userRepository.findAll(content.streamStrings("designers").toList()).stream().map(User::getFullName).collect(toList()),
                userRepository.findAll(content.streamStrings("coAuthors").toList()).stream().map(User::getFullName).collect(toList())
        ))));
    }

    private static List<AbstractPdfSection> reactionConverter(Pair<Component, Experiment> p) {
        return MongoExt.of(p.getLeft()).map(content -> singletonList(new ReactionSchemeSection(new ReactionSchemeModel(
                new SvgPdfImage(content.getString("image"))
        ))));
    }

    private static List<AbstractPdfSection> preferredCompoundSummaryConverter(Pair<Component, Experiment> p) {
        return MongoExt.of(p.getLeft()).map(content ->
                singletonList(new PreferredCompoundsSection(new PreferredCompoundsModel(
                        content.streamObjects("batches")
                                .map(ExperimentPdfSectionsProvider::getPreferredCompoundsRow)
                                .toList()))));
    }

    private static PreferredCompoundsRow getPreferredCompoundsRow(MongoExt compound) {
        MongoExt stereoisomerObj = compound.getObject("stereoisomer");
        Structure structure = new Structure(
                new SvgPdfImage(compound.getString("structure", "image")),
                compound.getString("virtualCompoundId"),
                stereoisomerObj.getString("name"),
                stereoisomerObj.getString("description"));
        return new PreferredCompoundsRow(
                structure,
                compound.getString("fullNbkBatch"),
                compound.getString("molWeight", "value"),
                compound.getString("formula"),
                compound.getString("structureComments")
        );
    }

    private static List<AbstractPdfSection> stoichTableConverter(Pair<Component, Experiment> p) {
        return MongoExt.of(p.getLeft()).map(content -> {
            List<StoichiometryRow> reactants = content.streamObjects("reactants")
                    .map(ExperimentPdfSectionsProvider::getStoichiometryModel)
                    .toList();
            return singletonList(new StoichiometrySection((new StoichiometryModel(reactants))));
        });
    }

    private static StoichiometryRow getStoichiometryModel(MongoExt reactant) {
        return new StoichiometryRow()
                .setFullNbkBatch(reactant.getString("fullNbkBatch"))
                .setCompoundId(reactant.getString("compoundId"))
                .setStructure(new StoichiometryModel.Structure(new SvgPdfImage(reactant.getString("structure", "image"))))
                .setMolecularWeight(reactant.getString("molWeight", "value"))
                .setWeight(reactant.getString("weight", "value"))
                .setWeightUnit(reactant.getString("weight", "unit"))
                .setMoles(reactant.getString("mol", "value"))
                .setMolesUnit(reactant.getString("mol", "unit"))
                .setVolume(reactant.getString("volume", "value"))
                .setVolumeUnit(reactant.getString("volume", "unit"))
                .setEq(reactant.getString("eq", "value"))
                .setChemicalName(reactant.getString("chemicalName"))
                .setRxnRole(reactant.getString("rxnRole", "name"))
                .setStoicPurity(reactant.getString("stoicPurity", "value"))
                .setMolarity(reactant.getString("molarity", "value"))
                .setMolesUnit(reactant.getString("molarity", "unit"))
                .setHazardComments(reactant.getString("hazardComments"))
                .setSaltCode(reactant.getString("saltCode", "name"))
                .setSaltEq(reactant.getString("saltEq", "value"))
                .setComments(reactant.getString("comments"));
    }

    private static List<AbstractPdfSection> experimentDescriptionConverter(Pair<Component, Experiment> p) {
        return MongoExt.of(p.getLeft()).map(content -> {
            String description = content.getString("description");
            return singletonList(new DescriptionSection(new DescriptionModel(description, "EXPERIMENT")));
        });
    }

    private List<AbstractPdfSection> productBatchSummaryConverter(Pair<Component, Experiment> p) {
        Optional<List<String>> batchOwner = p.getRight().getComponents().stream()
                .filter(component -> REACTION_DETAILS.equals(component.getName()))
                .map(MongoExt::of)
                .map(m -> userRepository.findAll(m.streamStrings("batchOwner").toList()).stream().map(User::getFullName).collect(toList()))
                .findAny();

        List<BatchInformationRow> batchInfoRows = MongoExt.of(p.getLeft())
                .streamObjects("batches")
                .map(batch -> getBatchInformationRow(batch, batchOwner)).toList();

        List<RegistrationSummaryRow> regSummaryRows = MongoExt.of(p.getLeft())
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
    }

    private static BatchInformationRow getBatchInformationRow(MongoExt batch, Optional<List<String>> batchOwner) {
        MongoExt stereoisomer = batch.getObject("stereoisomer");
        return new BatchInformationRow()
                .setNbkBatch(batch.getString("nbkBatch"))
                .setStructure(new BatchInformationModel.Structure(
                        new SvgPdfImage(batch.getString("structure", "image")),
                        stereoisomer.getString("name"),
                        stereoisomer.getString("description")
                ))
                .setAmountMade(batch.getString("totalWeight", "value"))
                .setAmountMadeUnit(batch.getString("totalWeight", "unit"))
                .setTheoWeight(batch.getString("theoWeight", "value"))
                .setTheoWeightUnit(batch.getString("theoWeight", "unit"))
                .setYield(batch.getString("yield"))
                .setPurity(batch.getString("purity", "asString"))
                .setBatchInformation(new BatchInformation(
                        batch.getString("molWeight", "value"),
                        batch.getString("exactMass"),
                        batch.getString("saltCode", "name"),
                        batch.getString("saltEq", "value"),
                        batchOwner.orElse(emptyList()),
                        batch.getString("comments")
                ));
    }

    private List<AbstractPdfSection> productBatchDetailsConverter(Pair<Component, Experiment> p) {
        Optional<MongoExt> content = p.getRight().getComponents().stream()
                .filter(component -> PRODUCT_BATCH_SUMMARY.equals(component.getName()))
                .map(Component::getContent)
                .map(MongoExt::of)
                .findAny();

        Optional<List<String>> batchOwner = p.getRight().getComponents().stream()
                .filter(component -> REACTION_DETAILS.equals(component.getName()))
                .map(MongoExt::of)
                .map(m -> userRepository.findAll(m.streamStrings("batchOwner").toList()).stream().map(User::getFullName).collect(toList()))
                .findAny();

        Optional<List<AbstractPdfSection>> sections = content.map(m -> m.streamObjects("batches")
                .map(batch -> (AbstractPdfSection) new BatchDetailsSection(new BatchDetailsModel()
                        .setFullNbkBatch(batch.getString("fullNbkBatch"))
                        .setRegistrationDate(batch.getDate("registrationDate"))
                        .setStructureComments(batch.getString("structureComments"))
                        .setSource(batch.getString("source", "name"))
                        .setSourceDetail(batch.getString("sourceDetail", "name"))
                        .setBatchOwner(batchOwner.orElse(emptyList()))
                        .setMolWeight(batch.getString("molWeight", "value"))
                        .setFormula(batch.getString("formula"))
                        .setResidualSolvent(batch.getString("residualSolvents", "asString"))
                        .setSolubility(batch.getString("solubility", "asString"))
                        .setPrecursors(batch.getString("precursors"))
                        .setExternalSupplier(batch.getString("externalSupplier", "asString"))
                        .setHealthHazards(batch.getString("healthHazards", "asString"))
                        .setHandlingPrecautions(batch.getString("handlingPrecautions", "asString"))
                        .setStorageInstructions(batch.getString("storageInstructions", "asString"))
                ))
                .toList());
        return sections.orElse(emptyList());
    }

    private List<GridFSDBFile> getFiles(Set<String> fileIds) {
        if (!fileIds.isEmpty()) {
            return fileRepository.findAll(fileIds, new PageRequest(0, fileIds.size())).getContent();
        } else {
            return emptyList();
        }
    }

    private AttachmentsSection getAttachmentsSection() {
        List<FileDTO> list = files.stream()
                .map(FileDTO::new)
                .collect(toList());
        return new AttachmentsSection(new AttachmentsModel(list));
    }

    @Override
    public List<InputStream> getExtraPdf() {
        if (printRequest.includeAttachments()) {
            return files.stream()
                    .filter(f -> "application/pdf".equals(f.getContentType()))
                    .map(GridFSDBFile::getInputStream)
                    .collect(toList());
        } else {
            return PdfSectionsProvider.super.getExtraPdf();
        }
    }

    public ExperimentHeaderSection getHeaderSection() {
        List<Component> components = experiment.getComponents();
        Optional<String> title1 = StreamEx.of(components)
                .findAny(c -> REACTION_DETAILS.equals(c.getName()))
                .map(Component::getContent)
                .map(MongoExt::of)
                .map(c -> c.getString("title"));
        Optional<String> title2 = StreamEx.of(components)
                .findAny(c -> CONCEPT_DETAILS.equals(c.getName()))
                .map(Component::getContent)
                .map(MongoExt::of)
                .map(c -> c.getString("title"));

        return new ExperimentHeaderSection(new ExperimentHeaderModel(
                LogoUtils.loadDefaultLogo(),
                Instant.now(),
                experiment.getAuthor().getFullName(),
                notebook.getName() + "-" + experiment.getFullName(),
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
        List<AbstractPdfSection> convert(Pair<Component, Experiment> p);
    }
}
