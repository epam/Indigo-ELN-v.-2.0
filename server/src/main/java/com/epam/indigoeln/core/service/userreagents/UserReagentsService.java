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
package com.epam.indigoeln.core.service.userreagents;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserReagents;
import com.epam.indigoeln.core.repository.userreagents.UserReagentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing user reagents.
 */
@Service
public class UserReagentsService {

    /**
     * UserReagentsRepository instance.
     */
    @Autowired
    private UserReagentsRepository userReagentsRepository;

    /**
     * Retrieves user reagents from DB for this user if user exists.
     *
     * @param user User for whom the reagents will be retrieved
     * @return Reagents for this user if user exists and {@code null} otherwise.
     */
    public List<Object> getUserReagents(User user) {
        final UserReagents userReagents = userReagentsRepository.findByUser(user);
        return userReagents == null ? null : userReagents.getReagents();
    }

    /**
     * Saves user reagents. Updates reagents if regents exists for this user or
     * creates new reagents if there are no reagents for this user.
     *
     * @param user     User for whom the reagents will be saved
     * @param reagents Reagents to save
     */
    public void saveUserReagents(User user, List reagents) {
        UserReagents userReagents = userReagentsRepository.findByUser(user);
        if (userReagents == null) {
            userReagents = new UserReagents();
            userReagents.setUser(user);
        }
        userReagents.setReagents(reagents);
        userReagentsRepository.save(userReagents);
    }
}
