package at.co.svc.agate.openapi.phase1.serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgateSerializedParameter {

    private final String name;

    private final List<String> values;


    public AgateSerializedParameter(
            String name,
            List<String> values) {

        this.name = name;

        this.values =
                values != null
                        ? new ArrayList<>(values)
                        : new ArrayList<>();
    }


    public String getName() {
        return name;
    }


    public List<String> getValues() {

        return Collections.unmodifiableList(
                values
        );
    }


    public boolean isEmpty() {

        return values.isEmpty();
    }


    public int size() {

        return values.size();
    }
}