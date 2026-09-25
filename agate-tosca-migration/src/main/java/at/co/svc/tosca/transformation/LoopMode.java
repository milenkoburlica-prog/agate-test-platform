package at.co.svc.tosca.transformation;

public enum LoopMode {

    /**
     * Tosca pre-condition loop:
     *
     * while (condition) {
     *     body;
     * }
     *
     * AGATE:
     *
     * condition
     * BREAK inverse(condition)
     * body
     */
    WHILE_DO,

    /**
     * Tosca post-condition loop:
     *
     * do {
     *     body;
     * } while (condition);
     *
     * AGATE:
     *
     * body
     * condition
     * BREAK inverse(condition)
     */
    DO_WHILE
}