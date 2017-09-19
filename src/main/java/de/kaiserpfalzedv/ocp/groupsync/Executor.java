package de.kaiserpfalzedv.ocp.groupsync;

/**
 * @author rlichti {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2017-09-12
 */
public interface Executor {
    /**
     * The basic command pattern ...
     *
     * @throws ExecuterException If an exception needs to be thrown it should be encapsulated within an
     *                           ExecuterException.
     */
    void execute() throws ExecuterException;
}
