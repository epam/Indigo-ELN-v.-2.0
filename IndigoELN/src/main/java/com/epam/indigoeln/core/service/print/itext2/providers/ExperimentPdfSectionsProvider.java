package com.epam.indigoeln.core.service.print.itext2.providers;

import com.epam.indigoeln.core.model.Component;
import com.epam.indigoeln.core.model.Experiment;
import com.epam.indigoeln.core.model.Notebook;
import com.epam.indigoeln.core.model.Project;
import com.epam.indigoeln.core.repository.file.FileRepository;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.indigoeln.core.service.print.itext2.model.experiment.PreferredCompoundsModel.Structure;
import static com.epam.indigoeln.core.util.BatchComponentUtil.*;
import static java.util.Collections.singletonList;

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

    private static final HashMap<String, ComponentToPdfSectionsConverter> componentNameToConverter = new HashMap<>();

    public ExperimentPdfSectionsProvider(Project project, Notebook notebook, Experiment experiment, FileRepository fileRepository,
                                         PrintRequest printRequest) {
        this.project = project;
        this.notebook = notebook;
        this.experiment = experiment;
        this.fileRepository = fileRepository;
        this.files = getFiles(experiment.getFileIds());
        this.printRequest = printRequest;
    }

    /**
     * @return list of raw uninitialized pdf sections corresponding to experiment components.
     */
    public List<AbstractPdfSection> getContentSections() {
        List<AbstractPdfSection> list = StreamEx.of(experiment.getComponents())
                .filter(c -> printRequest.getComponents().contains(c.getName()))
                .flatMap(this::sections)
                .toList();
        if (printRequest.getComponents().contains(ATTACHMENTS)) {
            list.add(getAttachmentsSection());
        }
        return list;
    }

    private Stream<AbstractPdfSection> sections(Component component) {
        return Optional.ofNullable(componentNameToConverter.get(component.getName()))
                .map(converter -> converter.convert(Pair.of(component, experiment)).stream())
                .orElseGet(Stream::empty);
    }

    static {
        put(REACTION_DETAILS, ExperimentPdfSectionsProvider::reactionDetailsConverter);
        put(CONCEPT_DETAILS, ExperimentPdfSectionsProvider::conceptDetailsConverter);
        put(REACTION, ExperimentPdfSectionsProvider::reactionConverter);
        put(PREFERRED_COMPOUND_SUMMARY, ExperimentPdfSectionsProvider::preferredCompoundSummaryConverter);
        put(STOICH_TABLE, ExperimentPdfSectionsProvider::stoichTableConverter);
        put(EXPERIMENT_DESCRIPTION, ExperimentPdfSectionsProvider::experimentDescriptionConverter);
        put(PRODUCT_BATCH_SUMMARY, ExperimentPdfSectionsProvider::productBatchSummaryConverter);
        put(PRODUCT_BATCH_DETAILS, ExperimentPdfSectionsProvider::productBatchDetailsConverter);
    }

    private static void put(String name, ComponentToPdfSectionsConverter builder) {
        componentNameToConverter.put(name, builder);
    }

    private static List<AbstractPdfSection> reactionDetailsConverter(Pair<Component, Experiment> p) {
        return MongoExt.of(p.getLeft()).map(content -> singletonList(new ReactionDetailsSection(new ReactionDetailsModel()
                .setCreationDate(p.getRight().getCreationDate())
                .setTherapeuticArea(content.getString("therapeuticArea", "name"))
                .setContinuedFrom(content.streamObjects("contFromRxn").map(m -> m.getString("text")).toList())
                .setContinuedTo(content.streamObjects("contToRxn").map(m -> m.getString("text")).toList())
                .setProjectCode(content.getString("codeAndName", "name"))
                .setProjectAlias(content.getString("projectAliasName"))
                .setLinkedExperiment(content.streamObjects("linkedExperiments").map(m -> m.getString("text")).toList())
                .setLiteratureReference(content.getString("literature"))
                .setCoauthors(content.streamObjects("coAuthors").map(m -> m.getString("name")).toList())
        )));
    }

    private static List<AbstractPdfSection> conceptDetailsConverter(Pair<Component, Experiment> p) {
        return MongoExt.of(p.getLeft()).map(content -> singletonList(new ConceptDetailsSection(new ConceptDetailsModel(
                p.getRight().getCreationDate(),
                content.getString("therapeuticArea", "name"),
                content.streamObjects("linkedExperiments").map(m -> m.getString("text")).toList(),
                content.getString("codeAndName", "name"),
                content.getString("keywords"),
                content.streamObjects("designers").map(m -> m.getString("name")).toList(),
                content.streamObjects("coAuthors").map(m -> m.getString("name")).toList()
        ))));
    }

    private static List<AbstractPdfSection> reactionConverter(Pair<Component, Experiment> p) {
        return MongoExt.of(p.getLeft()).map(content -> singletonList(new ReactionSchemeSection(new ReactionSchemeModel(
                new SvgPdfImage(content.getString("image"))
        ))));
    }

    private static List<AbstractPdfSection> preferredCompoundSummaryConverter(Pair<Component, Experiment> p) {
        return MongoExt.of(p.getLeft()).map(content -> {
            List<PreferredCompoundsRow> rows = content.streamObjects("compounds")
                    .map(ExperimentPdfSectionsProvider::getPreferredCompoundsRow)
                    .toList();
            return singletonList(new PreferedCompoundsSection(new PreferredCompoundsModel(rows)));
        });
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

    private static List<AbstractPdfSection> productBatchSummaryConverter(Pair<Component, Experiment> p) {
        Optional<List<String>> batchOwner = p.getRight().getComponents().stream()
                .filter(component -> REACTION_DETAILS.equals(component.getName()))
                .map(MongoExt::of)
                .map(m -> m.streamObjects("batchOwner").map(owner -> owner.getString("name")).toList())
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
                        batchOwner.orElse(Collections.emptyList()),
                        batch.getString("comments")
                ));
    }

    private static List<AbstractPdfSection> productBatchDetailsConverter(Pair<Component, Experiment> p) {
        Optional<MongoExt> content = p.getRight().getComponents().stream()
                .filter(component -> PRODUCT_BATCH_SUMMARY.equals(component.getName()))
                .map(Component::getContent)
                .map(MongoExt::of)
                .findAny();

        Optional<List<String>> batchOwner = p.getRight().getComponents().stream()
                .filter(component -> REACTION_DETAILS.equals(component.getName()))
                .map(MongoExt::of)
                .map(m -> m.streamObjects("batchOwner").map(owner -> owner.getString("name")).toList())
                .findAny();

        Optional<List<AbstractPdfSection>> sections = content.map(m -> m.streamObjects("batches")
                .map(batch -> (AbstractPdfSection) new BatchDetailsSection(new BatchDetailsModel()
                        .setFullNbkBatch(batch.getString("fullNbkBatch"))
                        .setRegistrationDate(batch.getString("registrationDate"))
                        .setStructureComments(batch.getString("structureComments"))
                        .setSource(batch.getString("source", "name"))
                        .setSourceDetail(batch.getString("sourceDetail", "name"))
                        .setBatchOwner(batchOwner.orElse(Collections.emptyList()))
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
        return sections.orElse(Collections.emptyList());
    }

    private List<GridFSDBFile> getFiles(Set<String> fileIds) {
        if (!fileIds.isEmpty()) {
            return fileRepository.findAll(fileIds, new PageRequest(0, fileIds.size())).getContent();
        } else {
            return Collections.emptyList();
        }
    }

    private AttachmentsSection getAttachmentsSection() {
        List<FileDTO> list = files.stream()
                .map(FileDTO::new)
                .collect(Collectors.toList());
        return new AttachmentsSection(new AttachmentsModel(list));
    }

    @Override
    public List<InputStream> getExtraPdf() {
        if (printRequest.includeAttachments()) {
            return files.stream()
                    .filter(f -> "application/pdf".equals(f.getContentType()))
                    .map(GridFSDBFile::getInputStream)
                    .collect(Collectors.toList());
        } else {
            return PdfSectionsProvider.super.getExtraPdf();
        }
    }

    public ExperimentHeaderSection getHeaderSection() {
        List<Component> components = experiment.getComponents();
        Optional<String> title1 = StreamEx.of(components)
                .findFirst(c -> c.getName().equals(REACTION_DETAILS))
                .map(Component::getContent)
                .map(MongoExt::of)
                .map(c -> c.getString("title"));
        Optional<String> title2 = StreamEx.of(components)
                .findFirst(c -> c.getName().equals(CONCEPT_DETAILS))
                .map(Component::getContent)
                .map(MongoExt::of)
                .map(c -> c.getString("title"));

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
        List<AbstractPdfSection> convert(Pair<Component, Experiment> p);
    }
}
