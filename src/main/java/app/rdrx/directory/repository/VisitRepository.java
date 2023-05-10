package app.rdrx.directory.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import app.rdrx.directory.model.repository.GroupedVisit;
import app.rdrx.directory.model.repository.RefVisit;

public interface VisitRepository extends JpaRepository<RefVisit, Long>, JpaSpecificationExecutor<RefVisit>{
    @Query("SELECT new app.rdrx.directory.model.repository.GroupedVisit( " + 
        " sum(v.count), cast(v.createdDate as date) "+
    ") FROM Visit v " + 
    "WHERE " +
        "cast(v.createdDate as date) BETWEEN :begin AND :end AND " +
        "v.reference.id = :refId " +
    "GROUP BY v.reference.id, cast(v.createdDate as date) ")
    public List<GroupedVisit> findVisitsOfRefInRange(
        @Param("refId") String refId, 
        @Param("begin") Date beginDate, 
        @Param("end") Date endDate
    );
}
