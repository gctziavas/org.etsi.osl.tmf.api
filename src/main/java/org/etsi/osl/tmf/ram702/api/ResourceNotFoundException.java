package org.etsi.osl.tmf.ram702.api;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(404, message);  // Setting the HTTP status code to 404 for "Not Found"
    }
}
