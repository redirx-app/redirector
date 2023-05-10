package app.rdrx.directory.model.repository;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;

@Entity(name="Visit")
public class RefVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="referenceId", nullable=false)
    private StoredReference reference;
    Date createdDate;
    private Long count;
    

    public RefVisit(){
        id = null;
        reference = null;
        createdDate = new Date();
        count = 1l;
    }

    public RefVisit(StoredReference reference){
        id = null;
        this.reference = reference;
        createdDate = new Date();
        count = 1l;
    }

    public RefVisit(StoredReference ref, Date date, long cnt){
        id = null;
        reference = ref;
        createdDate = date;
        count = cnt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StoredReference getReference() {
        return reference;
    }

    public void setReference(StoredReference reference) {
        this.reference = reference;
        this.id = null;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
        this.id = null;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "RefVisit [" + count + " @ " + createdDate + ", ref.aliases=" + reference.getAliases() + "]";
    }

    @PrePersist
    public void beforeInsert(){
        if(createdDate == null){
            createdDate = new Date();
        }
    }
}
