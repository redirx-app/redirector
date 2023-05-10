package app.rdrx.directory.model.repository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ReferenceOwner")
public class RefOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String owner;
    @ManyToOne
    @JoinColumn(name="referenceId", nullable=false)
    private StoredReference reference;

    public RefOwner(){
        this.id = null;
        this.owner = null;
        this.reference = null;
    }

    public RefOwner(Long id, String owner, StoredReference reference) {
        this.id = id;
        this.owner = owner;
        this.reference = reference;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public StoredReference getReference() {
        return reference;
    }
    public void setReference(StoredReference reference) {
        this.reference = reference;
    }
   

    
}
