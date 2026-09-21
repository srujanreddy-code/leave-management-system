package com.ust.lms.common;

import com.ust.lms.model.CommonFields;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

/**
 * Provides common service functionality for entity auditing and soft deletion.
 */
public abstract class CommonService {

    /**
     * Sets the audit fields of an entity based on whether it is being created
     * or modified.
     *
     * @param entity the entity whose audit fields are being set
     * @param isNew  indicates whether the entity is newly created
     */
    protected void setAuditFields(CommonFields entity, boolean isNew) {
        String currentUser = getCurrentUsername();
        LocalDateTime now = LocalDateTime.now();
        if (isNew) {
            entity.setCreatedBy(currentUser);
            entity.setCreatedOn(now);
            entity.setModifiedBy(null);
            entity.setModifiedOn(null);
        } else {
            entity.setModifiedBy(currentUser);
            entity.setModifiedOn(now);
        }
    }

    /**
     * Retrieves the username of the currently authenticated user.
     *
     * @return authenticated username, or "system" when no authenticated user is available
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        return authentication.getName();
    }

    /**
     * Performs a soft delete by marking the entity as deleted and inactive.
     *
     * @param entity the entity to be soft deleted
     */
    protected void softDelete(CommonFields entity) {
        entity.setDeleted(true);
        entity.setStatus(false);
    }
}