package app.rdrx.directory.model.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.ReadOnlyProperty;

public class Reference {

    public static final String ID_FIELD = "id";
    public static final String ALIASES_FIELD = "aliases";
    public static final String URL_SLICES_FIELD = "urlSlices";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String OWNERS_FIELD = "owners";
    public static final String TAGS_FIELD = "tags";
    public static final String PARAMS_FIELD = "params";

    private String id;
    private List<String> aliases;
    private List<PathSlice> urlSlices;
    private String description;
    private List<String> owners;
    private List<String> tags;
    private List<ReferenceParam> params;
    @ReadOnlyProperty
    private String primaryAlias;
    @ReadOnlyProperty
    private String defaultHref;
    @ReadOnlyProperty
    private Date createdDate;
    @ReadOnlyProperty
    private Date lastModifiedDate;

    public Reference(){
        this.id = null;
        this.description = null;
        this.defaultHref = null;
        this.aliases = new ArrayList<>();
        this.primaryAlias = null;
        this.owners = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.params = new ArrayList<>();
        this.urlSlices = new ArrayList<>();
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String getPrimaryAlias() {
        return primaryAlias;
    }

    public void setPrimaryAlias(String primaryAlias) {
        this.primaryAlias = primaryAlias;
    }

    public List<PathSlice> getUrlSlices() {
        return urlSlices;
    }

    public void setUrlSlices(List<PathSlice> urlSlices) {
        this.urlSlices = urlSlices;
        this.evaluateDefaultHref();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getOwners() {
        return owners;
    }

    public void setOwners(List<String> owners) {
        this.owners = owners;
    }

    public List<ReferenceParam> getParams() {
        return params;
    }

    public void setParams(List<ReferenceParam> params) {
        this.params = params;
        this.evaluateDefaultHref();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Reference { " + aliases + " -> " + defaultHref + " }";
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getDefaultHref() {
        return defaultHref;
    }

    public void setDefaultHref(String defaultHref) {
        this.defaultHref = defaultHref;
    }

    private void evaluateDefaultHref(){
        StringBuilder sb = new StringBuilder();
        if(this.urlSlices != null){
            for(PathSlice slice : this.urlSlices){
                switch(slice.getType()){
                    case PathSlice.TYPE_TEXT:
                        sb.append(slice.getValue());
                        break;
                    case PathSlice.TYPE_PARAM:
                        if(this.params != null){
                            for(ReferenceParam param : this.params){
                                if(slice.getValue().equals(param.getName())){
                                    sb.append(param.getDefaultValue());
                                    break;
                                }
                            }
                        }
                        break;
                    default:
                        continue; // just going to ignore unexpected types.
                }
            }
        }
        this.defaultHref = sb.toString();
    }
    
}
