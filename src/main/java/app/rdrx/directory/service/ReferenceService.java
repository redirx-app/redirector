package app.rdrx.directory.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import app.rdrx.directory.model.exceptions.DoesNotExistException;
import app.rdrx.directory.model.exceptions.RecordFieldErrors;
import app.rdrx.directory.model.repository.RefAlias;
import app.rdrx.directory.model.repository.RefTag;
import app.rdrx.directory.model.repository.RefVisit;
import app.rdrx.directory.model.repository.StoredReference;
import app.rdrx.directory.model.service.Reference;
import app.rdrx.directory.model.service.ReferenceDetail;
import app.rdrx.directory.repository.StoredReferenceRepository;
import app.rdrx.directory.repository.VisitRepository;

@Service
public class ReferenceService {

    Logger logger = LoggerFactory.getLogger(ReferenceService.class);

    private static final Integer DEFAULT_PAGE_SIZE = 100;

    private static final String REQUIRED_ERROR = "Required";
    private static final Set<String> RESERVED_ALIASES = new HashSet<>(Arrays.asList("app", "api", "new"));

    @Autowired
    private StoredReferenceRepository refRepo;
    @Autowired
    private VisitRepository visitRepo;

    public Reference updateReference(Reference ref, boolean partial) throws RecordFieldErrors, DoesNotExistException{
        RecordFieldErrors recordErrors = new RecordFieldErrors();
        try{
            validateReference(ref);
        } catch (RecordFieldErrors er) {
            recordErrors.addErrors(er);
        }
        if(ref.getId() == null || ref.getId().isBlank()){
            recordErrors.addFieldError(Reference.ID_FIELD, REQUIRED_ERROR);
        } else {

            StoredReference toBeUpdated = null;
            try{
                toBeUpdated = refRepo.findById(ref.getId()).orElseThrow();
            } catch (NoSuchElementException ex){
                throw new DoesNotExistException(ex);
            }
            List<Reference> existingRefs = this.getByAliases(ref.getAliases(), PageRequest.ofSize(DEFAULT_PAGE_SIZE));
            if(existingRefs.size() > 1){
                List<String> takenAliases = new ArrayList<>();
                existingRefs.forEach(r -> {
                    if(!r.getId().equals(ref.getId())){
                        r.getAliases().forEach(a -> {
                            if(ref.getAliases().contains(a)){
                                takenAliases.add(a);
                            }
                        });
                    }
                });
                if(!takenAliases.isEmpty()){
                    recordErrors.addFieldError(Reference.ALIASES_FIELD, this.alreadyTakenError(takenAliases));
                }
            }
    
            try{
                if(partial){
                    toBeUpdated = toBeUpdated.partialUpdateWith(ref);
                } else {
                    toBeUpdated = toBeUpdated.updateWith(ref);
                }
            } catch (RecordFieldErrors er){
                recordErrors.addErrors(er);
            }
            recordErrors.throwIfHasErrors();
            logger.info("Saving record {}", toBeUpdated.toReference());
            return refRepo.save(toBeUpdated).toReference();
        }
        throw recordErrors;
    }
    
    public Reference createReference(Reference ref) throws IllegalArgumentException, RecordFieldErrors{
        RecordFieldErrors recordErrors = new RecordFieldErrors();
        validateReference(ref);
        if(ref.getId() != null){
            recordErrors.addFieldError(Reference.ID_FIELD, "Cannot insert new reference with an ID.");
        }
        List<Reference> existingRefs = this.getByAliases(ref.getAliases(), PageRequest.ofSize(DEFAULT_PAGE_SIZE));
        if(!existingRefs.isEmpty()){
            List<String> takenAliases = new ArrayList<>();
            existingRefs.forEach(r -> takenAliases.addAll(r.getAliases()));
            if(!takenAliases.isEmpty()){
                recordErrors.addFieldError(Reference.ALIASES_FIELD, this.alreadyTakenError(takenAliases));
            }
            recordErrors.throwIfHasErrors();
        }
        return refRepo.save(new StoredReference(ref)).toReference();
        
    }

    private String alreadyTakenError(List<String> aliases){
        if(aliases == null){
            return "There is at least one alias already taken.";
        } else if(aliases.size()==1){
            return "The alias '" + aliases.get(0) + "' is already taken";
        } else if (aliases.size() > 1){
            return "The aliases " + aliases + " are already taken";
        } else {
            return null;
        }
    }

    public void deleteReference(String id) throws IllegalArgumentException{
        try{
            StoredReference ref = refRepo.findById(id).orElseThrow();
            List<RefVisit> visitsOfRef = visitRepo.findAll(new VisitsOfRef(ref));
            visitRepo.deleteAll(visitsOfRef);
            refRepo.delete(ref);
        } catch (NoSuchElementException ex){
            // ref doesn't exist. don't care.
        }
    }

    public Page<ReferenceDetail> search(List<String> queryValues, Integer pageSize, Integer page, String sortColumn, String sortOrder){
        if(page == null || page < 0){
            page = 0;
        }
        if(pageSize == null || pageSize < 1){
            pageSize = 100;
        }
        if(sortColumn == null || sortColumn.isBlank()){
            sortColumn = "createdDate";
        }
        if(sortOrder == null || sortOrder.isBlank()){
            sortOrder = "asc";
        }
        Page<StoredReference> queryResult;
        List<ReferenceDetail> result = new ArrayList<>();
        Pageable pageable = PageRequest.of(
            page, 
            pageSize, 
            Sort.by(
                Direction.valueOf(sortOrder.toUpperCase()),
                sortColumn
            )
        );
        if(queryValues == null || queryValues.isEmpty()){
            queryResult = refRepo.findAll(pageable);
        }else {
            queryResult = refRepo.findAll(hasAliasOrTagLikeThese(queryValues), pageable);
        } 
        queryResult.get().forEach(sRef -> result.add(sRef.toDetail()));
        //
        return new PageImpl<>(result, pageable, queryResult.getTotalElements());
    }

    public Optional<ReferenceDetail> getByAliasOrId(String aliasOrId) throws RecordFieldErrors{
        try{
            UUID.fromString(aliasOrId);
            // If this format fits a UUID pattern, search by uuid.
            return getById(aliasOrId);
        }catch(IllegalArgumentException ex){
            return getByAlias(aliasOrId);
        }
    }

    public Optional<ReferenceDetail> getByAlias(String alias) throws RecordFieldErrors{
        return getByAlias(alias, false);
    }

    public Optional<ReferenceDetail> getByAlias(String alias, boolean recordVisit) throws RecordFieldErrors{
        ReferenceDetail ref = null;
        try{
            ref = refRepo.findByAlias(alias).orElseThrow().toDetail();
            ref.setRecentVisits(visitRepo.findVisitsOfRefInRange(
                ref.getId(), 
                Date.from(ZonedDateTime.now().toInstant().minusSeconds(60l * 60l * 24l * 7l)), 
                Date.from(ZonedDateTime.now().toInstant())
            ));
            return Optional.of(ref);
        } catch (NoSuchElementException ex){
            return Optional.empty();
        } catch (IllegalArgumentException ex){
            RecordFieldErrors fieldErrors = new RecordFieldErrors();
            fieldErrors.addFieldError(Reference.ID_FIELD, REQUIRED_ERROR);
            throw fieldErrors;
        } finally {
            if(recordVisit && ref != null){
                try{
                    RefVisit newVisit = new RefVisit(new StoredReference(ref), new Date(), 1l);
                    visitRepo.saveAndFlush(newVisit);
                } catch (Exception ex) {
                    // oh well. 
                }
            }
        }
    }

    public List<Reference> getByAliases(List<String> aliases, PageRequest page){
        List<String> standardizedAliases = new ArrayList<>(aliases.size());
        aliases.forEach(alias -> standardizedAliases.add(alias.toLowerCase()));
        List<StoredReference> queryResult = refRepo.findByAliasesLower(standardizedAliases, page);
        List<Reference> references = new ArrayList<>(queryResult.size());
        queryResult.forEach(storedRef -> references.add(storedRef.toReference()));
        return references;
    }
    
    public Optional<ReferenceDetail> getById(String id) throws RecordFieldErrors{
        try{
            ReferenceDetail ref = refRepo.findById(id).orElseThrow().toDetail();
            ref.setRecentVisits(visitRepo.findVisitsOfRefInRange(
                ref.getId(), 
                Date.from(ZonedDateTime.now().toInstant().minusSeconds(60l * 60l * 24l * 7l)), 
                Date.from(ZonedDateTime.now().toInstant())
            ));
            return Optional.of(ref);
        } catch (NoSuchElementException ex){
            return Optional.empty();
        } catch (IllegalArgumentException ex){
            RecordFieldErrors fieldErrors = new RecordFieldErrors();
            fieldErrors.addFieldError(Reference.ID_FIELD, REQUIRED_ERROR);
            throw fieldErrors;
        }
    }

    public void validateReference(Reference ref) throws RecordFieldErrors{
        RecordFieldErrors recordErrors = new RecordFieldErrors();
        if(ref == null){
            recordErrors.addNonFieldError("Cannot save a null reference");
            throw recordErrors;
        }
        if(ref.getAliases() == null || ref.getAliases().isEmpty()){
            recordErrors.addFieldError(Reference.ALIASES_FIELD, "Must Provide at least one alias.");
        } else {
            ref.getAliases().forEach(alias ->{
                if(alias.length() < 2){
                    recordErrors.addFieldError(Reference.ALIASES_FIELD, "Cannot add alias '" + alias + "': must be at least 2 characters");
                } else if (RESERVED_ALIASES.contains(alias.toLowerCase())){
                    recordErrors.addFieldError(Reference.ALIASES_FIELD, "Cannot add alias '" + alias + "': '" + alias + "' is reserved");
                }
            });
        }
        if(ref.getOwners() == null || ref.getOwners().isEmpty()){
            recordErrors.addFieldError(Reference.OWNERS_FIELD, REQUIRED_ERROR);
        }
        recordErrors.throwIfHasErrors();
    }

    private static Specification<StoredReference> hasAliasOrTagLikeThese(List<String> searchParams){
        return new ReferencesWithAliasesOrTagsLikeIn(searchParams);
    }
}

final class VisitsOfRef implements Specification<RefVisit> {

    private String referenceId;

    public VisitsOfRef(Reference ref){
        this.referenceId = ref.getId();
    }

    public VisitsOfRef(StoredReference ref){
        this.referenceId = ref.getId();
    }

    public VisitsOfRef(String refId){
        this.referenceId = refId;
    }

    @Override
    public Predicate toPredicate(Root<RefVisit> root, CriteriaQuery<?> query, CriteriaBuilder cb){
        Join<RefVisit, StoredReference> refJoin = root.join("reference", JoinType.INNER);
        return cb.equal(refJoin.get("id"), this.referenceId);
    }
}


final class ReferencesWithAliasesOrTagsLikeIn implements Specification<StoredReference> {

    private List<String>searchParams;

    public ReferencesWithAliasesOrTagsLikeIn(List<String> search){
        this.searchParams = search;
    }

    @Override
    public Predicate toPredicate(Root<StoredReference> root, CriteriaQuery<?> query, CriteriaBuilder cb){
        Predicate result = cb.and(); // start as false.
        Join<StoredReference, RefAlias> aliasJoin = root.join(Reference.ALIASES_FIELD, JoinType.LEFT);
        Join<StoredReference, RefTag> tagJoin = root.join(Reference.TAGS_FIELD, JoinType.LEFT);
        for(String param : searchParams){
            result = cb.and(
                result, cb.or(
                    cb.like(cb.lower(aliasJoin.<String>get("name")), "%" + param.toLowerCase() + "%"),
                    cb.like(cb.lower(tagJoin.<String>get("tag")), "%" + param.toLowerCase() + "%")
                )
            );
        }

        query.distinct(true);
        
        return result;
    }
        
}