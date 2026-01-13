package com.epam.indigoeln.reaction.service.mutation;

import com.epam.indigoeln.eln.api.AccessForm;
import com.epam.indigoeln.eln.entity.AttachmentEntity;
import com.epam.indigoeln.eln.entity.UserEntity;
import com.epam.indigoeln.eln.model.AccessLevel;
import com.epam.indigoeln.eln.service.UserService;
import com.google.common.base.Preconditions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class EntityMutationHelper {

    @Inject
    UserService userService;

    public String formatEditAttributesSummary(List<String> summaryList) {
        Preconditions.checkState(!summaryList.isEmpty());
        return switch (summaryList.size()) {
            case 1 -> "Edit: " + summaryList.getFirst();
            case 2 -> "Edit: " + summaryList.get(0) + ", " + summaryList.get(1);
            default -> "Edit: multiple attributes";
        };
    }

    public String formatEditAccessSummary(List<AccessForm> edits) {
        if (edits.size() == 1) {
            AccessForm update = edits.getFirst();
            UserEntity user = userService.getUserEntity(update.getUserID());
            if (update.getLevel() == AccessLevel.NONE) {
                return "Edited Team: removed " + user.getUsername();
            } else {
                return "Edited Team: granted " + user.getUsername() + " " + update.getLevel() + " access";
            }
        } else {
            return "Edit Team: multiple updates";
        }
    }

    public String formatCreateAttachmentSummary(AttachmentEntity attachment) {
        return "Created attachment: %s, %d bytes".formatted(attachment.getName(), attachment.getSize());
    }
}
