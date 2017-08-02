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

    private final UserReagentsRepository userReagentsRepository;

    @Autowired
    public UserReagentsService(UserReagentsRepository userReagentsRepository) {
        this.userReagentsRepository = userReagentsRepository;
    }

    public BasicDBList getUserReagents(User user) {
        final UserReagents userReagents = userReagentsRepository.findByUser(user);
        return userReagents == null ? null : userReagents.getReagents();
    }

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