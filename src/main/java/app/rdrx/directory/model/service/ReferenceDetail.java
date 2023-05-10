package app.rdrx.directory.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.ReadOnlyProperty;

import app.rdrx.directory.model.repository.GroupedVisit;
public class ReferenceDetail extends Reference{
    @ReadOnlyProperty
    protected Map<String, Long> visitStats;
    @ReadOnlyProperty
    protected List<GroupedVisit> recentVisits;

    public ReferenceDetail(){
        super();
        visitStats = new HashMap<>();
        recentVisits = new ArrayList<>();
    }

    public Map<String, Long> getVisitStats() {
        return visitStats;
    }

    public void setVisitStats(Map<String, Long> visitStats) {
        this.visitStats = visitStats;
    }

    public List<GroupedVisit> getRecentVisits() {
        return recentVisits;
    }

    public void setRecentVisits(List<GroupedVisit> recentVisits) {
        this.recentVisits = recentVisits;
    }

    
}
