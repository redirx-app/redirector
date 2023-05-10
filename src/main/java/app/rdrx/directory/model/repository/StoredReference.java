package app.rdrx.directory.model.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import app.rdrx.directory.model.exceptions.RecordFieldErrors;
import app.rdrx.directory.model.service.PathSlice;
import app.rdrx.directory.model.service.Reference;
import app.rdrx.directory.model.service.ReferenceDetail;
import app.rdrx.directory.model.service.ReferenceParam;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;

@Entity(name="Reference")
public class StoredReference {

    private static final String PARAM_FIELD = "params";
    private static final String SLICES_FIELD = "urlSlices";
    private static final String REQUIRED_ERROR = "Required";

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name="id", columnDefinition="VARCHAR(255)")
    protected String id;
    protected String description;
    protected String pathSlicesJSON;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reference", orphanRemoval = true)
    protected Set<RefAlias> aliases;
    protected String primaryAlias;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reference", orphanRemoval = true)
    protected Set<RefOwner> owners;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reference", orphanRemoval = true)
    protected Set<RefTag> tags;
    protected String paramsJSON;
    protected Date createdDate;
    protected Date lastModifiedDate;
    @Formula(value = "("+
        "select sum(v.count) "+
        "FROM visit v " + 
        "WHERE " +
        "v.reference_id = id " +
    ")")
    protected Long visitsAll;
    @Formula(value = "("+
        "select sum(v.count) "+
        "FROM visit v " + 
        "WHERE " +
            "v.created_date >= DATEADD('DAY', -30, CURRENT_DATE()) " +
            "AND v.created_date <= DATEADD('DAY', 1, CURRENT_DATE()) " +
            "AND v.reference_id = id " +
    ")")
    protected Long visits30;
    @Formula(value = "("+
        "select sum(v.count) "+
        "FROM visit v " + 
        "WHERE " +
            "v.created_date >= DATEADD('DAY', -90, CURRENT_DATE()) " +
            "AND v.created_date <= DATEADD('DAY', 1, CURRENT_DATE()) " +
            "AND v.reference_id = id " +
    ")")
    protected Long visits90;


    public StoredReference(){
        this.id = null;
        this.description = null;
        this.pathSlicesJSON = "[]";
        this.aliases = new HashSet<>();
        this.primaryAlias = null;
        this.owners = new HashSet<>();
        this.tags = new HashSet<>();
        this.paramsJSON = "[]";
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
    }

    public StoredReference(StoredReference src){
        // Transform each property to break references.
        this.id = src.id;

        this.description = src.description;

        this.pathSlicesJSON = src.pathSlicesJSON;

        this.paramsJSON = src.paramsJSON;

        if(src.aliases == null){
            this.aliases = new HashSet<>();
        } else {
            this.aliases = new HashSet<>(src.aliases);
        }

        this.primaryAlias = src.primaryAlias;

        if(src.owners == null){
            this.owners = new HashSet<>();
        } else {
            this.owners = new HashSet<>(src.owners);
        }

        if(src.tags == null){
            this.tags = new HashSet<>();
        } else {
            this.tags = new HashSet<>(src.tags);
        }
        if(src.createdDate == null ){
            this.createdDate = new Date();
        } else {
            this.createdDate = Date.from(src.createdDate.toInstant());
        }

        if(src.lastModifiedDate == null){
            this.lastModifiedDate = new Date();
        } else {
            this.lastModifiedDate = Date.from(src.lastModifiedDate.toInstant());
        }
    }

    public StoredReference (Reference src) throws RecordFieldErrors{
        RecordFieldErrors recordErrors = new RecordFieldErrors();
        this.id = src.getId();
        try{
            this.setAliases(src.getAliases());
        } catch (RecordFieldErrors e){
            recordErrors.addErrors(e);
        }
        this.description = src.getDescription();
        this.setTags(src.getTags());
        this.setOwners(src.getOwners());
        try{
            this.setPathWithParams(src.getParams(), src.getUrlSlices());
        } catch (RecordFieldErrors e){
            recordErrors.addErrors(e);
        }
        recordErrors.throwIfHasErrors();
        this.createdDate = src.getCreatedDate();
        this.lastModifiedDate = src.getLastModifiedDate();
    }

    public StoredReference updateWith(Reference ref)throws RecordFieldErrors{
        StoredReference updatedRef = new StoredReference(this);
        RecordFieldErrors recordErrors = new RecordFieldErrors();
        
        if(updatedRef.id != null && !updatedRef.id.equals(ref.getId())){
            recordErrors.addFieldError("id", "ID Update mismatch");
        } else if (updatedRef.id == null) {
            updatedRef.id = ref.getId();
        }
        updatedRef.description = ref.getDescription();
        updatedRef.setPathWithParams(ref.getParams(), ref.getUrlSlices());

        if(ref.getAliases() == null || ref.getAliases().isEmpty()){
            recordErrors.addFieldError("aliases", REQUIRED_ERROR);
        } else {
            updatedRef.primaryAlias = ref.getAliases().get(0);
            updatedRef.aliases = new HashSet<>();
            boolean foundAliasMatch = false;
            for(String alias : ref.getAliases()){
                foundAliasMatch = false;
                for(RefAlias storedAlias : this.aliases){
                    if(storedAlias.getName().equals(alias)){
                        updatedRef.aliases.add(new RefAlias(storedAlias.getId(), storedAlias.getName(), updatedRef));
                        foundAliasMatch = true;
                        break;
                    }
                }
                if(!foundAliasMatch){
                    updatedRef.aliases.add(new RefAlias(null, alias, updatedRef));
                }
            }
        }
        
        if(ref.getOwners() == null || ref.getOwners().isEmpty()){
            recordErrors.addFieldError("owners", REQUIRED_ERROR);
        } else {
            updatedRef.owners = new HashSet<>();
            boolean foundOwnerMatch = false;
            for(String owner : ref.getOwners()){
                foundOwnerMatch = false;
                for(RefOwner storedOwner : this.owners){
                    if(storedOwner.getOwner().equals(owner)){
                        updatedRef.owners.add(new RefOwner(storedOwner.getId(), storedOwner.getOwner(), updatedRef));
                        foundOwnerMatch = true;
                        break;
                    }
                }
                if(!foundOwnerMatch){
                    updatedRef.owners.add(new RefOwner(null, owner, updatedRef));
                }
            }
        }

        if(ref.getTags() != null && !ref.getTags().isEmpty()){
            updatedRef.tags = new HashSet<>();
            boolean foundTagMatch = false;
            for(String tag : ref.getTags()){
                foundTagMatch = false;
                for(RefTag storedTag : this.tags){
                    if(storedTag.getTag().equalsIgnoreCase(tag)){
                        updatedRef.tags.add(new RefTag(storedTag.getId(), storedTag.getTag(), updatedRef));
                        foundTagMatch = true;
                        break;
                    }
                }
                if(!foundTagMatch){
                    updatedRef.tags.add(new RefTag(null, tag, updatedRef));
                }
            }
        }

        updatedRef.lastModifiedDate = new Date();
        recordErrors.throwIfHasErrors();
        return updatedRef;
    }

    public StoredReference partialUpdateWith(Reference ref)throws RecordFieldErrors{
        StoredReference updatedRef = new StoredReference(this);
        RecordFieldErrors recordErrors = new RecordFieldErrors();
        
        if(updatedRef.id != null && !updatedRef.id.equals(ref.getId())){
            recordErrors.addFieldError("id", "ID Update mismatch");
        } else if (updatedRef.id == null) {
            updatedRef.id = ref.getId();
        }
        if(ref.getDescription() != null && !ref.getDescription().isBlank()){
            updatedRef.description = ref.getDescription();
        }
        if(
            (ref.getUrlSlices() != null && !ref.getUrlSlices().isEmpty()) || 
            (ref.getParams()    != null && !ref.getParams().isEmpty())
        ){
            try{
                updatedRef.setPathWithParams(ref.getParams(), ref.getUrlSlices());
            } catch (RecordFieldErrors er){
                recordErrors.addErrors(er);
            }
        }

        if(ref.getAliases() != null && !ref.getAliases().isEmpty()){
            updatedRef.primaryAlias = ref.getAliases().get(0);
            updatedRef.aliases = new HashSet<>();
            boolean foundAliasMatch = false;
            for(String alias : ref.getAliases()){
                foundAliasMatch = false;
                for(RefAlias storedAlias : this.aliases){
                    if(storedAlias.getName().equals(alias)){
                        updatedRef.aliases.add(new RefAlias(storedAlias.getId(), storedAlias.getName(), updatedRef));
                        foundAliasMatch = true;
                        break;
                    }
                }
                if(!foundAliasMatch){
                    updatedRef.aliases.add(new RefAlias(null, alias, updatedRef));
                }
                if(updatedRef.primaryAlias == null || updatedRef.primaryAlias.isBlank()){
                    updatedRef.primaryAlias = alias;
                }
            }

        }
        
        if(ref.getOwners() != null && !ref.getOwners().isEmpty()){
            updatedRef.owners = new HashSet<>();
            boolean foundOwnerMatch = false;
            for(String owner : ref.getOwners()){
                foundOwnerMatch = false;
                for(RefOwner storedOwner : this.owners){
                    if(storedOwner.getOwner().equals(owner)){
                        updatedRef.owners.add(new RefOwner(storedOwner.getId(), storedOwner.getOwner(), updatedRef));
                        foundOwnerMatch = true;
                        break;
                    }
                }
                if(!foundOwnerMatch){
                    updatedRef.owners.add(new RefOwner(null, owner, updatedRef));
                }
            }
        }

        if(ref.getTags() == null || ref.getTags().isEmpty()){
            updatedRef.tags = new HashSet<>();
            boolean foundTagMatch = false;
            for(String tag : ref.getTags()){
                foundTagMatch = false;
                for(RefTag storedTag : this.tags){
                    if(storedTag.getTag().equalsIgnoreCase(tag)){
                        updatedRef.tags.add(new RefTag(storedTag.getId(), storedTag.getTag(), updatedRef));
                        foundTagMatch = true;
                        break;
                    }
                }
                if(!foundTagMatch){
                    updatedRef.tags.add(new RefTag(null, tag, updatedRef));
                }
            }
        }

        updatedRef.lastModifiedDate = new Date();
        recordErrors.throwIfHasErrors();
        return updatedRef;
    }

    public Reference toReference() throws JsonSyntaxException{
        Gson gson = new Gson();
        Reference result = new Reference();
        result.setId(id);
        result.setAliases(getAliases());
        result.setPrimaryAlias(primaryAlias);
        result.setUrlSlices(Arrays.asList(gson.fromJson(this.pathSlicesJSON, PathSlice[].class)));
        result.setDescription(description);
        result.setParams(Arrays.asList(gson.fromJson(this.paramsJSON, ReferenceParam[].class)));
        result.setOwners(getOwners());
        result.setTags(getTags());
        result.setCreatedDate(createdDate);
        result.setLastModifiedDate(lastModifiedDate);

        return result;
    }

    public ReferenceDetail toDetail() throws JsonSyntaxException {
        Gson gson = new Gson();
        ReferenceDetail result = new ReferenceDetail();
        result.setId(id);
        result.setAliases(getAliases());
        result.setPrimaryAlias(primaryAlias);
        result.setUrlSlices(Arrays.asList(gson.fromJson(this.pathSlicesJSON, PathSlice[].class)));
        result.setDescription(description);
        result.setParams(Arrays.asList(gson.fromJson(this.paramsJSON, ReferenceParam[].class)));
        result.setOwners(getOwners());
        result.setTags(getTags());
        result.setCreatedDate(createdDate);
        result.setLastModifiedDate(lastModifiedDate);
        result.getVisitStats().put("all", visitsAll);
        result.getVisitStats().put("30", visits30);
        result.getVisitStats().put("90", visits90);
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getAliases() {
        List<String> stringAliases = new ArrayList<>();
        aliases.forEach(alias -> stringAliases.add(alias.getName()));
        return stringAliases;
    }

    public void setAliases(List<String> aliases) throws RecordFieldErrors{
        RecordFieldErrors errors = new RecordFieldErrors();
        if(aliases == null || aliases.isEmpty()){
            errors.addFieldError("aliases", "You must provide at least one alias.");
            throw errors;
        }
        primaryAlias = aliases.get(0);
        ArrayList<String> newAliases = new ArrayList<>(aliases);
        Set<RefAlias> oldAliases = this.aliases;
        this.aliases = new HashSet<>();
        if(oldAliases != null){
            oldAliases.forEach(alias -> {
                if(newAliases.contains(alias.getName())){
                    this.aliases.add(alias);
                    newAliases.remove(alias.getName());
                }
            });
        }
        newAliases.forEach(alias -> this.aliases.add(new RefAlias(null, alias, this)));
    }

    public void setAliases(Set<RefAlias> aliases) {
        this.aliases = aliases;
        if(aliases != null && !aliases.isEmpty()){
            this.primaryAlias = (aliases.toArray(new RefAlias[0]))[0].getName();
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        List<String> stringTags = new ArrayList<>(this.tags.size());
        this.tags.forEach(tag -> stringTags.add(tag.getTag()));
        return stringTags;
    }

    public void setTags(Set<RefTag> tags){
        this.tags = tags;
    }

    public void setTags(List<String> tags) {
        ArrayList<String> newTags = new ArrayList<>(tags);
        Set<RefTag> oldTags = this.tags;
        this.tags = new HashSet<>();
        if(oldTags != null){
            oldTags.forEach(tag -> {
                if(newTags.contains(tag.getTag())){
                    this.tags.add(tag);
                    newTags.remove(tag.getTag());
                }
            });
        }
        newTags.forEach(tag -> this.tags.add(new RefTag(null, tag, this)));
    }

    public String getParamsJSON() {
        return paramsJSON;
    }

    public void setParamsJSON(String paramsJSON) {
        this.paramsJSON = paramsJSON;
    }

    private void setPathWithParams(List<ReferenceParam> params, List<PathSlice> slices) throws RecordFieldErrors{
        RecordFieldErrors errors = new RecordFieldErrors();
        Set<String> pathSliceParamNames = new HashSet<>();

        if(slices == null || slices.isEmpty()){
            errors.addFieldError(SLICES_FIELD, REQUIRED_ERROR);
        } else {
            short textSliceCount = 0;
            for(int i = 0; i < slices.size(); i++){
                PathSlice slice = slices.get(i);
                if(PathSlice.TYPE_PARAM.equals(slice.getType())){
                    pathSliceParamNames.add(slice.getValue());
                } else if (PathSlice.TYPE_TEXT.equals(slice.getType())){
                    textSliceCount++;
                }

                if(slice.getValue() == null || slice.getValue().isBlank()){
                    errors.addFieldError(SLICES_FIELD, "Slice number [" + (i+1) + "] must include a [value]" );
                }
            }
            if(textSliceCount == 0){
                errors.addFieldError(SLICES_FIELD, "Must include at least 1 text slice.");
            }
        }

        if(!pathSliceParamNames.isEmpty()){
            if(params != null){
                for(ReferenceParam param : params){
                    if(param.getName() == null){
                        final String errorText = "Each param must have a name";
                        if(errors.getFieldErrors().getOrDefault(PARAM_FIELD, new ArrayList<>(0)).contains(errorText)){
                            errors.addFieldError(PARAM_FIELD, errorText);
                        }
                    } else if(!pathSliceParamNames.remove(param.getName())){
                        errors.addFieldError(PARAM_FIELD, "Extra param '" + param.getName() + "' is not used in the path.");
                    }
                }
            }
            pathSliceParamNames.forEach(param -> errors.addFieldError(PARAM_FIELD, "Missing param field: [" + param + "]"));
        }


        errors.throwIfHasErrors();
        Gson gson = new Gson();
        this.paramsJSON = gson.toJson(params);
        this.pathSlicesJSON = gson.toJson(slices);
    }

    public List<String> getOwners() {
        List<String> stringOwners = new ArrayList<>();
        
        owners.forEach(owner -> stringOwners.add(owner.getOwner()));
        return stringOwners;
    }

    public void setOwners(List<String> owners) {
        ArrayList<String> newOwners = new ArrayList<>(owners);
        Set<RefOwner> oldOwners = this.owners;
        this.owners = new HashSet<>();
        if(oldOwners != null){
            oldOwners.forEach(owner -> {
                if(newOwners.contains(owner.getOwner())){
                    this.owners.add(owner);
                    newOwners.remove(owner.getOwner());
                }
            });
        }
        newOwners.forEach(owner -> this.owners.add(new RefOwner(null, owner, this)));
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Long getVisitsAll() {
        return visitsAll;
    }

    public void setVisitsAll(Long visitsAll) {
        this.visitsAll = visitsAll;
    }

    public Long getVisits30() {
        return visits30;
    }

    public void setVisits30(Long visits30) {
        this.visits30 = visits30;
    }

    public Long getVisits90() {
        return visits90;
    }

    public void setVisits90(Long visits90) {
        this.visits90 = visits90;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((createdDate == null) ? 0 : createdDate.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((lastModifiedDate == null) ? 0 : lastModifiedDate.hashCode());
        result = prime * result + ((paramsJSON == null) ? 0 : paramsJSON.hashCode());
        result = prime * result + ((pathSlicesJSON == null) ? 0 : pathSlicesJSON.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StoredReference other = (StoredReference) obj;
        if (createdDate == null) {
            if (other.createdDate != null)
                return false;
        } else if (!createdDate.equals(other.createdDate))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (lastModifiedDate == null) {
            if (other.lastModifiedDate != null)
                return false;
        } else if (!lastModifiedDate.equals(other.lastModifiedDate))
            return false;
        if (paramsJSON == null) {
            if (other.paramsJSON != null)
                return false;
        } else if (!paramsJSON.equals(other.paramsJSON))
            return false;
        if (pathSlicesJSON == null) {
            if (other.pathSlicesJSON != null)
                return false;
        } else if (!pathSlicesJSON.equals(other.pathSlicesJSON))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StoredReference [aliases=" + aliases + "]";
    }

    @PrePersist
    public void beforeInsert(){
        if(this.primaryAlias == null || this.primaryAlias.isBlank()){
            this.primaryAlias = this.getAliases().get(0);
        }
        this.createdDate = this.lastModifiedDate = new Date();
    }

    @PreUpdate
    public void beforeUpdate(){
        if(this.primaryAlias == null || this.primaryAlias.isBlank()){
            this.primaryAlias = this.getAliases().get(0);
        }
        this.lastModifiedDate = new Date();
    }
}