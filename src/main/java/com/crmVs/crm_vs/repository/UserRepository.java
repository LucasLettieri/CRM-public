package com.crmVs.crm_vs.repository;

import com.crmVs.crm_vs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByTenantId(Long tenantId);
    User findByIdAndTenantId(Long id, Long tenantId);
    

    // CTE recursivo para encontrar todos los subordinados de un usuario
    @Query(value = """
    WITH RECURSIVE subordinados(id) AS (
    
        SELECT id
        FROM usuarios
        WHERE id = :jefeId
        
        UNION ALL
        
        SELECT u.id
        FROM usuarios u
        INNER JOIN subordinados s ON u.jefe_id = s.id
    )
    SELECT id FROM subordinados
    """, nativeQuery = true)
    List<Long> findAllSubordinadosIds(@Param("jefeId") Long jefeId);

    List<User> findByJefeIdAndTenantId(Long jefeId, Long tenantId);
}
