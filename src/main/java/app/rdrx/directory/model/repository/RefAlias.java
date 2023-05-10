package app.rdrx.directory.model.repository;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ReferenceAlias", 
    indexes = {
        @Index(name = "AliasNameIndex", columnList = "name", unique = true)
    }
)
public class RefAlias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne
    private StoredReference reference;

    public RefAlias() {
        this.id = null;
        this.name = null;
        this.reference = null;
    }

    public RefAlias(Long id, String name, StoredReference reference) {
        this.id = id;
        this.name = name;
        this.reference = reference;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public StoredReference getReference() {
        return reference;
    }
    public void setReference(StoredReference reference) {
        this.reference = reference;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        RefAlias other = (RefAlias) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }


    
}
