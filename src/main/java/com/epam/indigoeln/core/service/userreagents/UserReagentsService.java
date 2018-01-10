package com.epam.indigoeln.core.service.userreagents;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.model.UserReagents;
import com.epam.indigoeln.core.repository.userreagents.UserReagentsRepository;
import com.mongodb.BasicDBList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public BasicDBList getUserReagents(User user) {
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
    public void saveUserReagents(User user, BasicDBList reagents) {
        UserReagents userReagents = userReagentsRepository.findByUser(user);
        if (userReagents == null) {
            userReagents = new UserReagents();
            userReagents.setUser(user);
        }
        userReagents.setReagents(reagents);
        userReagentsRepository.save(userReagents);
    }
}
