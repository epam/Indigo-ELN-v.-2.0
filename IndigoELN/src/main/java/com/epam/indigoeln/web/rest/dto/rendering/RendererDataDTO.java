package com.epam.indigoeln.web.rest.dto.rendering;

import java.io.Serializable;

/**
 * Simple transfer object for Renderer data
 */
public class RendererDataDTO implements Serializable {

    private static final long serialVersionUID = -1206123763592253810L;

    Integer width; // image width to render with
    Integer height; // image height to render with
    String structure; // molfile with mol or rxn format

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

}
