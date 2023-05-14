package com.ays.organization.repository;

import com.ays.organization.model.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * An interface for accessing and managing organizations in a data source with CRUD operations.
 * It extends the JpaRepository interface with OrganizationEntity as the entity type and String as the ID type.
 * The default behavior of the repository can be extended by adding custom methods to this interface.
 */
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String> {
}