/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 * 
 * This file is part of Indigo Signature Service.
 * 
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
package com.chemistry.enotebook.signature.entity;

import java.util.UUID;

public class MockEntities {
    private static User mockAuthor;
    private static User mockWitness;
    public static User currentUser;

    static {
        mockAuthor = new User();
        mockAuthor.setUserId(UUID.fromString("d8fd0f74-33ac-4c84-b8ec-af5863dea123"));
        mockAuthor.setUsername("dukeSom");
        mockAuthor.setFirstName("Duke");
        mockAuthor.setLastName("Som");
        mockAuthor.setEmail("ds@email.com");
        mockAuthor.setAdmin(true);
        mockAuthor.setActive(true);

        mockWitness = new User();
        mockWitness.setUserId(UUID.fromString("d8fd0f74-33ac-4c84-b8ec-af5863dea124"));
        mockWitness.setUsername("koleSnider");
        mockWitness.setFirstName("Kole");
        mockWitness.setLastName("Snider");
        mockWitness.setEmail("ks@email.com");
        mockWitness.setAdmin(false);
        mockWitness.setActive(true);

        currentUser = mockAuthor;
    }
}
