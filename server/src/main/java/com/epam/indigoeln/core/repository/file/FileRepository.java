/*
 *  Copyright (C) 2015-2018 EPAM Systems
 *  
 *  This file is part of Indigo ELN.
 *
 *  Indigo ELN is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Indigo ELN is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.indigoeln.core.repository.file;

import com.epam.indigoeln.core.model.User;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.SneakyThrows;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.where;

@Repository
public class FileRepository {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    public GridFsResource store(InputStream content, String filename, String contentType, User author, boolean temporary) {
        ObjectId objectID = gridFsTemplate.store(content, filename, contentType, getMetadata(author, temporary));
        return gridFsTemplate.getResource(filename);
    }

    @SneakyThrows
    public void updateMetadata(GridFsResource file, Document metadata) {
        byte[] content = file.getContentAsByteArray();
        gridFsTemplate.store(new ByteArrayInputStream(content), file.getFilename(), metadata);
    }

    public GridFsResource findOneById(String id) {
        GridFSFile file = gridFsTemplate.findOne(query(where("_id").is(id)));
        return file != null ? gridFsTemplate.getResource(file) : null;
    }

    // !!! ALLOW REWRITE METADATA FOR A FILE
    public List<GridFsResource> findTemporary(Collection<String> ids) {
        return findResources(query(where("metadata.temporary").is(true)).addCriteria(where("_id").in(ids)));
    }

    public List<GridFsResource> findAllTemporary() {
        return findResources(query(where("metadata.temporary").is(true)));
    }

    public Page<GridFsResource> findAll(Collection<String> ids, Pageable pageable) {
        // gridFsTemplate uses a driver that doesn't know about "sort", "skip", "limit"
        // pageable is not suitable for this, therefore pagination have to be in memory
        List<GridFsResource> list = findResources(query(where("_id").in(ids)));

        // Mongo driver returns Unmodifiable list which cannot be sorted
        list = new ArrayList<>(list);

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

    private List<GridFsResource> findResources(Query query) {
        GridFSFindIterable it = gridFsTemplate.find(query);
        return FluentIterable.from(it).transform(file -> gridFsTemplate.getResource(file)).toList();
    }

    private Document getMetadata(User author, boolean temporary) {
        Document metadata = new Document();
        // Adding metadata about "author"
        GridFSFileUtil.setAuthorToMetadata(metadata, author);
        // Adding metadata flag if file is temporary (not attached to entity)
        GridFSFileUtil.setTemporaryToMetadata(metadata, temporary);
        return metadata;
    }

    private List<GridFsResource> applyPageable(List<GridFsResource> files, Pageable pageable) {
        Ordering<GridFsResource> ordering = null;
        if (pageable.getSort() != null) {
            for (Sort.Order order : pageable.getSort()) {
                Optional<Comparator<GridFsResource>> optional = GridFSFileUtil.getComparator(order.getProperty());
                if (optional.isPresent()) {
                    Comparator<GridFsResource> comparator = order.isAscending()
                            ? optional.get() : optional.get().reversed();
                    ordering = ordering != null ? ordering.compound(comparator) : Ordering.from(comparator);
                }
            }
        }
        if (ordering == null) {
            ordering = Ordering.from(GridFSFileUtil.UPLOAD_DATE_COMPARATOR.reversed());
        }
        files.sort(ordering);

        return files.stream().skip(pageable.getOffset()).limit(pageable.getPageSize())
                .collect(Collectors.toList());
    }
}
