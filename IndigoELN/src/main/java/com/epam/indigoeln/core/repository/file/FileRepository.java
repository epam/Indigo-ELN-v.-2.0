package com.epam.indigoeln.core.repository.file;

import com.epam.indigoeln.core.model.User;
import com.google.common.collect.Ordering;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.where;

@Repository
public class FileRepository {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    public GridFSFile store(InputStream content, String filename, String contentType, User author) {
        return gridFsTemplate.store(content, filename, contentType, getMetadata(author));
    }

    public GridFSDBFile findOneById(String id) {
        return gridFsTemplate.findOne(query(where("_id").is(id)));
    }

    public Page<GridFSDBFile> findAll(Collection<String> ids, Pageable pageable) {
        // gridFsTemplate uses a driver that doesn't know about "sort", "skip", "limit"
        // pageable is not suitable for this, therefore pagination have to be in memory
        List<GridFSDBFile> list = gridFsTemplate.find(query(where("_id").in(ids)));
        long totalCount = list.size();

        list = applyPageable(list, pageable);
        return new PageImpl<>(list, pageable, totalCount);
    }

    public void delete(String id) {
        gridFsTemplate.delete(query(where("_id").is(id)));
    }

    public void delete(Collection<String> ids) {
        gridFsTemplate.delete(query(where("_id").in(ids)));
    }

    private DBObject getMetadata(User author) {
        DBObject metadata = new BasicDBObject(1);
        // Adding metadata about "author"
        GridFSFileUtil.setAuthorToMetadata(metadata, author);

        return metadata;
    }

    private List<GridFSDBFile> applyPageable(List<GridFSDBFile> files, Pageable pageable) {
        Ordering<GridFSFile> ordering = null;
        if (pageable.getSort() != null) {
            for (Sort.Order order : pageable.getSort()) {
                Optional<Comparator<GridFSFile>> optional = GridFSFileUtil.getComparator(order.getProperty());
                if (optional.isPresent()) {
                    Comparator<GridFSFile> comparator = order.isAscending() ?
                            optional.get() : optional.get().reversed();
                    ordering = ordering != null ? ordering.compound(comparator) : Ordering.from(comparator);
                }
            }
        }
        if (ordering == null) {
            ordering = Ordering.from(GridFSFileUtil.UPLOAD_DATE_COMPARATOR.reversed());
        }
        Collections.sort(files, ordering);

        return files.stream().skip(pageable.getOffset()).limit(pageable.getPageSize())
                .collect(Collectors.toList());
    }
}