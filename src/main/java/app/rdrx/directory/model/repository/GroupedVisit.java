package app.rdrx.directory.model.repository;

import java.util.Date;

public class GroupedVisit {
    
    private Long totalVisits;
    private Date date;

    public GroupedVisit(long visits) {
        totalVisits = visits;
        date = null;
    }

    public GroupedVisit(long visits, Date createdDate){
        totalVisits = visits;
        date = createdDate;
    }

    public Long getTotalVisits() {
        return totalVisits;
    }

    public void setTotalVisits(Long totalVisits) {
        this.totalVisits = totalVisits;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
