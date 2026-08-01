package at.co.svc.agate.core.dsl.model;

/**
 * Defines the types of operations supported by the DSL.
 */
public enum StepType {
    CMD, 
    SQL, 
    OC,
    REST, SOAP, 
    GUI, 
    WAIT, 
    CALL,
    BUFFER,
    JSON,
    PDF
    //FILE,
    //STRING
}
