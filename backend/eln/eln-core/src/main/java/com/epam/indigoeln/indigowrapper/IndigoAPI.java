package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IndigoAPI {

    <T> T withSession(Function<IndigoSession, T> block);

    void withSession(Consumer<IndigoSession> block);

    IndigoObject iterateSDFile(String filename);

    IndigoObject iterateSDF(IndigoObject reader);

    void removeTautomerRule(int id);

    IndigoObject rgroupComposition(IndigoObject molecule, String options);

    int countReferences();

    IndigoObject writeBuffer();

    String checkStructure(String str);

    IndigoObject exactMatch(IndigoObject obj1, IndigoObject obj2);

    IndigoObject loadQueryMoleculeFromFile(String path);

    float similarity(IndigoObject obj1, IndigoObject obj2);

    IndigoObject createFileSaver(String filename, String format);

    IndigoObject loadHelm(String str, IndigoObject library);

    IndigoObject createQueryReaction();

    void dbgBreakpoint();

    IndigoObject loadReactionSmarts(String str);

    IndigoObject createReaction();

    boolean getOptionBool(String option);

    IndigoObject loadIdtFromFile(String path, IndigoObject library);

    String getUserSpecifiedPath();

    @Deprecated
    IndigoObject unserialize(byte[] data);

    IndigoObject reactionProductEnumerate(IndigoObject reaction, IndigoObject monomers);

    IndigoObject deserialize(byte[] data);

    IndigoObject loadMonomerLibrary(String str);

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

    IndigoObject iterateCDXFile(String filename);

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

    IndigoObject loadQueryReaction(String str);

    IndigoObject createArray();

    IndigoObject createQueryMolecule();

    IndigoObject loadStructureFromBuffer(byte[] buf);

    IndigoObject loadSmarts(byte[] buf);

    IndigoObject transform(IndigoObject reaction, IndigoObject monomer);

    IndigoObject transformHELMtoSCSR(IndigoObject item);

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

    void setSessionID();

    IndigoObject nameToStructure(String name, String params);

    void setTautomerRule(int id, String beg, String end);

    IndigoObject loadString(String string);

    IndigoObject loadSmartsFromFile(String path);

    IndigoObject loadFasta(String str, String seq_type, IndigoObject library);

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

    long getSid();

    IndigoObject loadStructureFromBuffer(byte[] buf, String params);

    IndigoObject reactionProductEnumerate(IndigoObject reaction, Iterable<Iterable<IndigoObject>> monomers);

    byte[] renderToBuffer(IndigoObject obj);

    void renderResetSettings();

    byte[] renderGridToBuffer(IndigoObject objects, int[] refAtoms, int ncolumns);

    void renderToFile(IndigoObject obj, String filename);

    void renderGridToFile(IndigoObject objects, int[] refAtoms, int ncolumns, String filename);

    void render(IndigoObject obj, IndigoObject output);

}
