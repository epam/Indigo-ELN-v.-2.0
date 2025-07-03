package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class IndigoAPIImpl implements IndigoAPI {

    private final Indigo indigo;
    private final IndigoRenderer renderer;

    @Override
    public <T> T withSession(Function<IndigoSession, T> block) {
        synchronized (indigo) {
            IndigoSession session = new IndigoSession(indigo, renderer);
            return block.apply(session);
        }
    }

    @Override
    public void withSession(Consumer<IndigoSession> block) {
        withSession(indigoSession -> {
            block.accept(indigoSession);
            return null;
        });
    }

    @Override
    public synchronized IndigoObject iterateSDFile(String filename) {
        return indigo.iterateSDFile(filename);
    }

    @Override
    public synchronized IndigoObject iterateSDF(IndigoObject reader) {
        return indigo.iterateSDF(reader);
    }

    @Override
    public synchronized void removeTautomerRule(int id) {
        indigo.removeTautomerRule(id);
    }

    @Override
    public synchronized IndigoObject rgroupComposition(IndigoObject molecule, String options) {
        return indigo.rgroupComposition(molecule, options);
    }

    @Override
    public synchronized int countReferences() {
        return indigo.countReferences();
    }

    @Override
    public synchronized IndigoObject writeBuffer() {
        return indigo.writeBuffer();
    }

    @Override
    public synchronized String checkStructure(String str) {
        return indigo.checkStructure(str);
    }

    @Override
    public synchronized IndigoObject exactMatch(IndigoObject obj1, IndigoObject obj2) {
        return indigo.exactMatch(obj1, obj2);
    }

    @Override
    public synchronized IndigoObject loadQueryMoleculeFromFile(String path) {
        return indigo.loadQueryMoleculeFromFile(path);
    }

    @Override
    public synchronized float similarity(IndigoObject obj1, IndigoObject obj2) {
        return indigo.similarity(obj1, obj2);
    }

    @Override
    public synchronized IndigoObject createFileSaver(String filename, String format) {
        return indigo.createFileSaver(filename, format);
    }

    @Override
    public synchronized IndigoObject loadHelm(String str, IndigoObject library) {
        return indigo.loadHelm(str, library);
    }

    @Override
    public synchronized IndigoObject createQueryReaction() {
        return indigo.createQueryReaction();
    }

    @Override
    public synchronized void dbgBreakpoint() {
        indigo.dbgBreakpoint();
    }

    @Override
    public synchronized IndigoObject loadReactionSmarts(String str) {
        return indigo.loadReactionSmarts(str);
    }

    @Override
    public synchronized IndigoObject createReaction() {
        return indigo.createReaction();
    }

    @Override
    public synchronized boolean getOptionBool(String option) {
        return indigo.getOptionBool(option);
    }

    @Override
    public synchronized IndigoObject loadIdtFromFile(String path, IndigoObject library) {
        return indigo.loadIdtFromFile(path, library);
    }

    @Override
    public synchronized String getUserSpecifiedPath() {
        return indigo.getUserSpecifiedPath();
    }

    @Deprecated
    @Override
    public synchronized IndigoObject unserialize(byte[] data) {
        return indigo.unserialize(data);
    }

    @Override
    public synchronized IndigoObject reactionProductEnumerate(IndigoObject reaction, IndigoObject monomers) {
        return indigo.reactionProductEnumerate(reaction, monomers);
    }

    @Override
    public synchronized IndigoObject deserialize(byte[] data) {
        return indigo.deserialize(data);
    }

    @Override
    public synchronized IndigoObject loadMonomerLibrary(String str) {
        return indigo.loadMonomerLibrary(str);
    }

    @Override
    public synchronized IndigoObject loadFastaFromFile(String path, String seq_type, IndigoObject library) {
        return indigo.loadFastaFromFile(path, seq_type, library);
    }

    @Override
    public synchronized String check(String str, String type, String params) {
        return indigo.check(str, type, params);
    }

    @Override
    public synchronized IndigoObject extractCommonScaffold(Collection<IndigoObject> structures, String options) {
        return indigo.extractCommonScaffold(structures, options);
    }

    @Override
    public synchronized String checkStructure(String str, String params) {
        return indigo.checkStructure(str, params);
    }

    @Override
    public synchronized IndigoObject iterateRDFile(String filename) {
        return indigo.iterateRDFile(filename);
    }

    @Override
    public synchronized IndigoObject loadSmarts(String str) {
        return indigo.loadSmarts(str);
    }

    @Override
    public synchronized IndigoObject iterateRDF(IndigoObject reader) {
        return indigo.iterateRDF(reader);
    }

    @Override
    public synchronized void clearTautomerRules() {
        indigo.clearTautomerRules();
    }

    @Override
    public synchronized IndigoObject createSaver(IndigoObject output, String format) {
        return indigo.createSaver(output, format);
    }

    @Override
    public synchronized String versionInfo() {
        return indigo.versionInfo();
    }

    @Override
    public synchronized IndigoObject loadSequence(String str, String seq_type, IndigoObject library) {
        return indigo.loadSequence(str, seq_type, library);
    }

    @Override
    public synchronized IndigoObject iterateCDXFile(String filename) {
        return indigo.iterateCDXFile(filename);
    }

    @Override
    public synchronized IndigoObject loadStructure(byte[] buf) {
        return indigo.loadStructure(buf);
    }

    @Override
    public synchronized IndigoObject loadQueryMolecule(String str) {
        return indigo.loadQueryMolecule(str);
    }

    @Override
    public synchronized int commonBits(IndigoObject fingerprint1, IndigoObject fingerprint2) {
        return indigo.commonBits(fingerprint1, fingerprint2);
    }

    @Deprecated
    @Override
    public synchronized IndigoObject decomposeMolecules(IndigoObject scaffold, Collection<IndigoObject> structures) {
        return indigo.decomposeMolecules(scaffold, structures);
    }

    @Override
    public synchronized IndigoObject iterateSmiles(IndigoObject reader) {
        return indigo.iterateSmiles(reader);
    }

    @Override
    public synchronized Float getOptionFloat(String option) {
        return indigo.getOptionFloat(option);
    }

    @Override
    public synchronized IndigoObject substructureMatcher(IndigoObject target) {
        return indigo.substructureMatcher(target);
    }

    @Override
    public synchronized IndigoObject loadIdt(String str, IndigoObject library) {
        return indigo.loadIdt(str, library);
    }

    @Override
    public synchronized IndigoObject loadStructure(String str) {
        return indigo.loadStructure(str);
    }

    @Override
    public synchronized IndigoObject loadMonomerLibraryFromFile(String path) {
        return indigo.loadMonomerLibraryFromFile(path);
    }

    @Override
    public synchronized int buildPkaModel(int level, float threshold, String filename) {
        return indigo.buildPkaModel(level, threshold, filename);
    }

    @Override
    public synchronized IndigoObject loadQueryReaction(String str) {
        return indigo.loadQueryReaction(str);
    }

    @Override
    public synchronized IndigoObject createArray() {
        return indigo.createArray();
    }

    @Override
    public synchronized IndigoObject createQueryMolecule() {
        return indigo.createQueryMolecule();
    }

    @Override
    public synchronized IndigoObject loadStructureFromBuffer(byte[] buf) {
        return indigo.loadStructureFromBuffer(buf);
    }

    @Override
    public synchronized IndigoObject loadSmarts(byte[] buf) {
        return indigo.loadSmarts(buf);
    }

    @Override
    public synchronized IndigoObject transform(IndigoObject reaction, IndigoObject monomer) {
        return indigo.transform(reaction, monomer);
    }

    @Override
    public synchronized IndigoObject transformHELMtoSCSR(IndigoObject item) {
        return indigo.transformHELMtoSCSR(item);
    }

    @Override
    public synchronized String version() {
        return indigo.version();
    }

    @Override
    public synchronized IndigoObject loadStructureFromFile(String path) {
        return indigo.loadStructureFromFile(path);
    }

    @Override
    public synchronized IndigoObject iterateSmilesFile(String filename) {
        return indigo.iterateSmilesFile(filename);
    }

    @Override
    public synchronized IndigoObject loadSequenceFromFile(String path, String seq_type, IndigoObject library) {
        return indigo.loadSequenceFromFile(path, seq_type, library);
    }

    @Override
    public synchronized IndigoObject iterateCML(IndigoObject reader) {
        return indigo.iterateCML(reader);
    }

    @Deprecated
    @Override
    public synchronized IndigoObject decomposeMolecules(IndigoObject scaffold, IndigoObject structures) {
        return indigo.decomposeMolecules(scaffold, structures);
    }

    @Override
    public synchronized IndigoObject loadReactionSmarts(byte[] buf) {
        return indigo.loadReactionSmarts(buf);
    }

    @Override
    public synchronized IndigoObject substructureMatcher(IndigoObject target, String mode) {
        return indigo.substructureMatcher(target, mode);
    }

    @Override
    public synchronized String getOptionType(String option) {
        return indigo.getOptionType(option);
    }

    @Override
    public synchronized IndigoObject loadStructure(String str, String params) {
        return indigo.loadStructure(str, params);
    }

    @Override
    public synchronized IndigoObject iterateTautomers(IndigoObject molecule, String params) {
        return indigo.iterateTautomers(molecule, params);
    }

    @Override
    public synchronized IndigoObject loadKetDocument(String str) {
        return indigo.loadKetDocument(str);
    }

    @Override
    public synchronized IndigoObject loadQueryReaction(byte[] buf) {
        return indigo.loadQueryReaction(buf);
    }

    @Override
    public synchronized IndigoObject loadFingerprintFromBuffer(byte[] buf) {
        return indigo.loadFingerprintFromBuffer(buf);
    }

    @Override
    public synchronized IndigoObject extractCommonScaffold(IndigoObject structures, String options) {
        return indigo.extractCommonScaffold(structures, options);
    }

    @Override
    public synchronized IndigoObject loadBuffer(byte[] buf) {
        return indigo.loadBuffer(buf);
    }

    @Override
    public synchronized IndigoObject nameToStructure(String name) {
        return indigo.nameToStructure(name);
    }

    @Override
    public synchronized String getOption(String option) {
        return indigo.getOption(option);
    }

    @Override
    public synchronized void setSessionID() {
        indigo.setSessionID();
    }

    @Override
    public synchronized IndigoObject nameToStructure(String name, String params) {
        return indigo.nameToStructure(name, params);
    }

    @Override
    public synchronized void setTautomerRule(int id, String beg, String end) {
        indigo.setTautomerRule(id, beg, end);
    }

    @Override
    public synchronized IndigoObject loadString(String string) {
        return indigo.loadString(string);
    }

    @Override
    public synchronized IndigoObject loadSmartsFromFile(String path) {
        return indigo.loadSmartsFromFile(path);
    }

    @Override
    public synchronized IndigoObject loadFasta(String str, String seq_type, IndigoObject library) {
        return indigo.loadFasta(str, seq_type, library);
    }

    @Override
    public synchronized IndigoObject createMolecule() {
        return indigo.createMolecule();
    }

    @Override
    public synchronized IndigoObject loadStructureFromFile(String path, String params) {
        return indigo.loadStructureFromFile(path, params);
    }

    @Override
    public synchronized IndigoObject getFragmentedMolecule(IndigoObject molecule, String options) {
        return indigo.getFragmentedMolecule(molecule, options);
    }

    @Override
    public synchronized boolean sessionReleased() {
        return indigo.sessionReleased();
    }

    @Override
    public synchronized IndigoObject writeFile(String filename) {
        return indigo.writeFile(filename);
    }

    @Override
    public synchronized IndigoObject loadHelmFromFile(String path, IndigoObject library) {
        return indigo.loadHelmFromFile(path, library);
    }

    @Override
    public synchronized IndigoObject loadReactionSmartsFromFile(String path) {
        return indigo.loadReactionSmartsFromFile(path);
    }

    @Override
    public synchronized IndigoObject exactMatch(IndigoObject obj1, IndigoObject obj2, String flags) {
        return indigo.exactMatch(obj1, obj2, flags);
    }

    @Override
    public synchronized IndigoObject iterateCMLFile(String filename) {
        return indigo.iterateCMLFile(filename);
    }

    @Override
    public synchronized IndigoObject iterateCDX(IndigoObject reader) {
        return indigo.iterateCDX(reader);
    }

    @Override
    public synchronized IndigoObject loadQueryMolecule(byte[] buf) {
        return indigo.loadQueryMolecule(buf);
    }

    @Override
    public synchronized float similarity(IndigoObject obj1, IndigoObject obj2, String metrics) {
        return indigo.similarity(obj1, obj2, metrics);
    }

    @Override
    public synchronized IndigoObject loadStructure(byte[] buf, String params) {
        return indigo.loadStructure(buf, params);
    }

    @Override
    public synchronized IndigoObject loadKetDocumentFromFile(String path) {
        return indigo.loadKetDocumentFromFile(path);
    }

    @Override
    public synchronized IndigoObject loadQueryReactionFromFile(String path) {
        return indigo.loadQueryReactionFromFile(path);
    }

    @Override
    public synchronized IndigoObject toIndigoArray(Collection<IndigoObject> coll) {
        return indigo.toIndigoArray(coll);
    }

    @Override
    public synchronized IndigoObject loadFingerprintFromDescriptors(double[] descriptors, int size, double density) {
        return indigo.loadFingerprintFromDescriptors(descriptors, size, density);
    }

    @Override
    public synchronized IndigoObject createDecomposer(IndigoObject scaffold) {
        return indigo.createDecomposer(scaffold);
    }

    @Override
    public synchronized Integer getOptionInt(String option) {
        return indigo.getOptionInt(option);
    }

    @Override
    public synchronized long getSid() {
        return indigo.getSid();
    }

    @Override
    public synchronized IndigoObject loadStructureFromBuffer(byte[] buf, String params) {
        return indigo.loadStructureFromBuffer(buf, params);
    }

    @Override
    public synchronized IndigoObject reactionProductEnumerate(IndigoObject reaction, Iterable<Iterable<IndigoObject>> monomers) {
        return indigo.reactionProductEnumerate(reaction, monomers);
    }

    @Override
    public synchronized byte[] renderToBuffer(IndigoObject obj) {
        return renderer.renderToBuffer(obj);
    }

    @Override
    public synchronized void renderResetSettings() {
        renderer.renderResetSettings();
    }

    @Override
    public synchronized byte[] renderGridToBuffer(IndigoObject objects, int[] refAtoms, int ncolumns) {
        return renderer.renderGridToBuffer(objects, refAtoms, ncolumns);
    }

    @Override
    public synchronized void renderToFile(IndigoObject obj, String filename) {
        renderer.renderToFile(obj, filename);
    }

    @Override
    public synchronized void renderGridToFile(IndigoObject objects, int[] refAtoms, int ncolumns, String filename) {
        renderer.renderGridToFile(objects, refAtoms, ncolumns, filename);
    }

    @Override
    public synchronized void render(IndigoObject obj, IndigoObject output) {
        renderer.render(obj, output);
    }
}
