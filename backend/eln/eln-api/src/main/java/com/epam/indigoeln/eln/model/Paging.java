package com.epam.indigoeln.eln.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paging {

    public static final int DEFAULT_PAGE_NO = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final Paging DEFAULT = new Paging(DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE);
    public static final Paging ALL = new Paging(0, Integer.MAX_VALUE);

    @QueryParam("pageNo")
    @Nullable
    @PositiveOrZero
    private Integer pageNo;
    @QueryParam("pageSize")
    @Nullable
    @Positive
    private Integer pageSize;

    @JsonIgnore
    public int getPageNoOrDefault() {
        return pageNo == null ? DEFAULT_PAGE_NO : pageNo;
    }

    @JsonIgnore
    public int getPageSizeOrDefault() {
        return pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
    }
}
