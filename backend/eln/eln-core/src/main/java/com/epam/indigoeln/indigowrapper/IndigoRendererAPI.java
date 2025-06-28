package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;
import lombok.RequiredArgsConstructor;

public interface IndigoRendererAPI {

    byte[] renderToBuffer(IndigoObject obj);

    void renderResetSettings();

    byte[] renderGridToBuffer(IndigoObject objects, int[] refAtoms, int ncolumns);

    void renderToFile(IndigoObject obj, String filename);

    void renderGridToFile(IndigoObject objects, int[] refAtoms, int ncolumns, String filename);

    void render(IndigoObject obj, IndigoObject output);

    @RequiredArgsConstructor
    class Impl implements IndigoRendererAPI{
        
        private final IndigoRenderer renderer;

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
}
