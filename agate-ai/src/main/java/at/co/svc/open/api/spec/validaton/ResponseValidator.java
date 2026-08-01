package at.co.svc.open.api.spec.validaton;

import at.co.svc.open.api.spec.model.EndpointDescription;
import at.co.svc.open.api.spec.model.GeneratedTestCase;

public class ResponseValidator {

    public boolean isAllowed(
            GeneratedTestCase tc,
            EndpointDescription endpoint) {


        String desc = tc.getDescription();

        if (desc == null) {
            desc = "";
        }

        desc = desc.toLowerCase();


        String expected = tc.getExpectedResult();


        if (desc.contains("not found")
                && !expected.equals("HTTP 404")) {

            return false;
        }


        if (desc.contains("bad request")
                && !expected.equals("HTTP 400")) {

            return false;
        }


        if (desc.contains("service unavailable")
                && !expected.equals("HTTP 503")) {

            return false;
        }


        return true;
    }
    }