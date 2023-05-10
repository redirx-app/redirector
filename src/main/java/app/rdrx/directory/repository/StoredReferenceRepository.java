package app.rdrx.directory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.rdrx.directory.model.repository.StoredReference;

public interface StoredReferenceRepository extends JpaRepository<StoredReference, String>, JpaSpecificationExecutor<StoredReference> {
    @Query("SELECT r FROM Reference r JOIN r.aliases a WHERE LOWER(a.name) = LOWER(:alias)")
    Optional<StoredReference> findByAlias(@Param("alias") String alias);
    
    @Query("SELECT r FROM Reference r JOIN r.aliases a WHERE LOWER(a.name) IN :aliases")
    List<StoredReference> findByAliasesLower(@Param("aliases") List<String> aliases, Pageable pageable);
}
