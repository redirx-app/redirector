package app.rdrx.directory.model.repository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="tags", indexes = {
    @Index(name="TagNameIndex", columnList="tag")
})
public class RefTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="referenceId", nullable=false)
    private StoredReference reference;
    @Column(nullable=false)
    private String tag;

    public RefTag(){
        this.id = null;
        this.reference = null;
        this.tag = null;
    }

    public RefTag(Long id, String tag,StoredReference reference) {
        this.id = id;
        this.reference = reference;
        this.tag = tag;
    }
    public StoredReference getReference() {
        return reference;
    }
    public void setReference(StoredReference reference) {
        this.reference = reference;
    }
    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
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
        RefTag other = (RefTag) obj;
        if (tag == null) {
            if (other.tag != null)
                return false;
        } else if (!tag.equals(other.tag))
            return false;
        return true;
    }

    
}
