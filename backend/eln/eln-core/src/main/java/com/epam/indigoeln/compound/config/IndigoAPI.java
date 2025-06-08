package com.epam.indigoeln.compound.config;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

public interface IndigoAPI {

    IndigoObject iterateSDFile(String filename);

    IndigoObject iterateSDF(IndigoObject reader);

    void removeTautomerRule(int id);

    IndigoObject rgroupComposition(IndigoObject molecule, String options);

    void setOption(String option, float value);

    int countReferences();

    IndigoObject writeBuffer();

    String checkStructure(String str);

    IndigoObject exactMatch(IndigoObject obj1, IndigoObject obj2);

    IndigoObject loadQueryMoleculeFromFile(String path);

    float similarity(IndigoObject obj1, IndigoObject obj2);

    IndigoObject createFileSaver(String filename, String format);

    void resetOptions();

    IndigoObject loadHelm(String str, IndigoObject library);

    IndigoObject createQueryReaction();

    void dbgBreakpoint();

    IndigoObject loadReactionSmarts(String str);

    IndigoObject createReaction();

    boolean getOptionBool(String option);

    IndigoObject loadIdtFromFile(String path, IndigoObject library);

    String getUserSpecifiedPath();

    IndigoObject loadMoleculeFromBuffer(byte[] buf);

    @Deprecated
    IndigoObject unserialize(byte[] data);

    IndigoObject reactionProductEnumerate(IndigoObject reaction, IndigoObject monomers);

    void setOption(String option, int x, int y);

    IndigoObject deserialize(byte[] data);

    IndigoObject loadMonomerLibrary(String str);

    IndigoObject loadReactionFromFile(String path);

    IndigoObject loadFastaFromFile(String path, String seq_type, IndigoObject library);

    String check(String str, String type, String params);

    IndigoObject extractCommonScaffold(Collection<IndigoObject> structures, String options);

    String checkStructure(String str, String params);

    IndigoObject iterateRDFile(String filename);

    IndigoObject loadSmarts(String str);

    IndigoObject iterateRDF(IndigoObject reader);

    void clearTautomerRules();

    IndigoObject createSaver(IndigoObject output, String format);

    String versionInfo();

    IndigoObject loadSequence(String str, String seq_type, IndigoObject library);

    void setOption(String option, boolean value);

    IndigoObject iterateCDXFile(String filename);

    void setOption(String option, float r, float g, float b);

    IndigoObject loadStructure(byte[] buf);

    IndigoObject loadQueryMolecule(String str);

    int commonBits(IndigoObject fingerprint1, IndigoObject fingerprint2);

    @Deprecated
    IndigoObject decomposeMolecules(IndigoObject scaffold, Collection<IndigoObject> structures);

    IndigoObject iterateSmiles(IndigoObject reader);

    Float getOptionFloat(String option);

    IndigoObject substructureMatcher(IndigoObject target);

    IndigoObject loadIdt(String str, IndigoObject library);

    IndigoObject loadStructure(String str);

    IndigoObject loadMonomerLibraryFromFile(String path);

    int buildPkaModel(int level, float threshold, String filename);

    IndigoObject loadMolecule(String str);

    IndigoObject loadQueryReaction(String str);

    void setOption(String option, int value);

    IndigoObject createArray();

    IndigoObject createQueryMolecule();

    IndigoObject loadStructureFromBuffer(byte[] buf);

    IndigoObject loadSmarts(byte[] buf);

    void setOption(String option, String value);

    IndigoObject transform(IndigoObject reaction, IndigoObject monomer);

    IndigoObject transformHELMtoSCSR(IndigoObject item);

    IndigoObject loadReaction(String str);

    String version();

    IndigoObject loadStructureFromFile(String path);

    IndigoObject iterateSmilesFile(String filename);

    IndigoObject loadSequenceFromFile(String path, String seq_type, IndigoObject library);

    IndigoObject iterateCML(IndigoObject reader);

    @Deprecated
    IndigoObject decomposeMolecules(IndigoObject scaffold, IndigoObject structures);

    IndigoObject loadReactionSmarts(byte[] buf);

    IndigoObject substructureMatcher(IndigoObject target, String mode);

    String getOptionType(String option);

    IndigoObject loadStructure(String str, String params);

    IndigoObject iterateTautomers(IndigoObject molecule, String params);

    IndigoObject loadKetDocument(String str);

    IndigoObject loadQueryReaction(byte[] buf);

    IndigoObject loadFingerprintFromBuffer(byte[] buf);

    IndigoObject extractCommonScaffold(IndigoObject structures, String options);

    IndigoObject loadBuffer(byte[] buf);

    IndigoObject nameToStructure(String name);

    String getOption(String option);

    IndigoObject loadMolecule(byte[] buf);

    void setSessionID();

    void setOption(String option, double value);

    IndigoObject nameToStructure(String name, String params);

    void setTautomerRule(int id, String beg, String end);

    IndigoObject loadString(String string);

    IndigoObject loadSmartsFromFile(String path);

    IndigoObject loadFasta(String str, String seq_type, IndigoObject library);

    IndigoObject loadReaction(byte[] buf);

    IndigoObject createMolecule();

    IndigoObject loadStructureFromFile(String path, String params);

    IndigoObject getFragmentedMolecule(IndigoObject molecule, String options);

    boolean sessionReleased();

    IndigoObject writeFile(String filename);

    IndigoObject loadHelmFromFile(String path, IndigoObject library);

    IndigoObject loadReactionSmartsFromFile(String path);

    IndigoObject exactMatch(IndigoObject obj1, IndigoObject obj2, String flags);

    IndigoObject iterateCMLFile(String filename);

    IndigoObject iterateCDX(IndigoObject reader);

    IndigoObject loadQueryMolecule(byte[] buf);

    float similarity(IndigoObject obj1, IndigoObject obj2, String metrics);

    IndigoObject loadStructure(byte[] buf, String params);

    IndigoObject loadKetDocumentFromFile(String path);

    IndigoObject loadQueryReactionFromFile(String path);

    IndigoObject toIndigoArray(Collection<IndigoObject> coll);

    IndigoObject loadFingerprintFromDescriptors(double[] descriptors, int size, double density);

    IndigoObject createDecomposer(IndigoObject scaffold);

    Integer getOptionInt(String option);

    IndigoObject loadMoleculeFromFile(String path);

    long getSid();

    IndigoObject loadStructureFromBuffer(byte[] buf, String params);

    IndigoObject reactionProductEnumerate(IndigoObject reaction, Iterable<Iterable<IndigoObject>> monomers);

    @RequiredArgsConstructor
    class Impl implements IndigoAPI {

        @Getter
        private final Indigo indigo;

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
        public synchronized void setOption(String option, float value) {
            indigo.setOption(option, value);
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
        public synchronized void resetOptions() {
            indigo.resetOptions();
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

        @Override
        public synchronized IndigoObject loadMoleculeFromBuffer(byte[] buf) {
            return indigo.loadMoleculeFromBuffer(buf);
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
        public synchronized void setOption(String option, int x, int y) {
            indigo.setOption(option, x, y);
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
        public synchronized IndigoObject loadReactionFromFile(String path) {
            return indigo.loadReactionFromFile(path);
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
        public synchronized void setOption(String option, boolean value) {
            indigo.setOption(option, value);
        }

        @Override
        public synchronized IndigoObject iterateCDXFile(String filename) {
            return indigo.iterateCDXFile(filename);
        }

        @Override
        public synchronized void setOption(String option, float r, float g, float b) {
            indigo.setOption(option, r, g, b);
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
        public synchronized IndigoObject loadMolecule(String str) {
            return indigo.loadMolecule(str);
        }

        @Override
        public synchronized IndigoObject loadQueryReaction(String str) {
            return indigo.loadQueryReaction(str);
        }

        @Override
        public synchronized void setOption(String option, int value) {
            indigo.setOption(option, value);
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
        public synchronized void setOption(String option, String value) {
            indigo.setOption(option, value);
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
        public synchronized IndigoObject loadReaction(String str) {
            return indigo.loadReaction(str);
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
        public synchronized IndigoObject loadMolecule(byte[] buf) {
            return indigo.loadMolecule(buf);
        }

        @Override
        public synchronized void setSessionID() {
            indigo.setSessionID();
        }

        @Override
        public synchronized void setOption(String option, double value) {
            indigo.setOption(option, value);
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
        public synchronized IndigoObject loadReaction(byte[] buf) {
            return indigo.loadReaction(buf);
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
        public synchronized IndigoObject loadMoleculeFromFile(String path) {
            return indigo.loadMoleculeFromFile(path);
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
    }
}
