package com.epam.indigoeln.test;

import java.util.List;

/** Reads the atom block of a V2000 molfile, without Indigo. */
public final class MolfileTestUtil {

    private MolfileTestUtil() {
    }

    /** The x and y of each atom, as written. */
    public static List<String> atomCoordinates(String molfile) {
        List<String> lines = molfile.lines().toList();
        return lines.subList(4, 4 + atomCount(lines)).stream()
                .map(line -> line.substring(0, 20).trim().replaceAll("\\s+", " "))
                .toList();
    }

    private static int atomCount(List<String> lines) {
        return Integer.parseInt(lines.get(3).substring(0, 3).trim());
    }
}
