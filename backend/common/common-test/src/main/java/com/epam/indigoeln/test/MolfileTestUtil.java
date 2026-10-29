package com.epam.indigoeln.test;

import java.util.ArrayList;
import java.util.List;

/** Reads and rewrites the atom block of a V2000 molfile, without Indigo. */
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

    /** The same molfile with every x, y and z set to zero, as a structure without a layout is stored. */
    public static String withoutCoordinates(String molfile) {
        List<String> lines = new ArrayList<>(molfile.lines().toList());
        for (int i = 4; i < 4 + atomCount(lines); i++) {
            lines.set(i, "    0.0000    0.0000    0.0000" + lines.get(i).substring(30));
        }
        return String.join("\n", lines) + "\n";
    }

    private static int atomCount(List<String> lines) {
        return Integer.parseInt(lines.get(3).substring(0, 3).trim());
    }
}
