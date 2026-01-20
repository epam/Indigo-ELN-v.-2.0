package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoRenderer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IndigoRendererAPI {

    private final Indigo indigo;
    private final IndigoRenderer renderer;

    public byte[] renderToBuffer(AbstractIndigoObject obj) {
        return renderer.renderToBuffer(obj.obj);
    }

    public void setRenderOptions(String format, int width, int height) {
        indigo.setOption("render-output-format", format);
        indigo.setOption("render-image-size", width, height);
    }
}
