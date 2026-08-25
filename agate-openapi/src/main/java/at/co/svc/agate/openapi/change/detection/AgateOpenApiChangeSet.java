package at.co.svc.agate.openapi.change.detection;

import java.util.ArrayList;
import java.util.List;


public class AgateOpenApiChangeSet {


    private final List<AgateApiChange> changes =
            new ArrayList<>();




    public void addChange(
            AgateApiChange change) {

        if (change == null) {

            return;
        }


        changes.add(
                change
        );
    }




    public List<AgateApiChange> getChanges() {

        return new ArrayList<>(
                changes
        );
    }




    public int size() {

        return changes.size();
    }




    public boolean isEmpty() {

        return changes.isEmpty();
    }




    public long countBreaking() {

        return changes
                .stream()
                .filter(change ->
                        change.getSeverity()
                                == AgateChangeSeverity.BREAKING
                )
                .count();
    }




    public long countReview() {

        return changes
                .stream()
                .filter(change ->
                        change.getSeverity()
                                == AgateChangeSeverity.REVIEW
                )
                .count();
    }
}