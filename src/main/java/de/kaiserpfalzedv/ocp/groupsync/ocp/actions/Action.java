package de.kaiserpfalzedv.ocp.groupsync.ocp.actions;

import de.kaiserpfalzedv.ocp.groupsync.ExecuterException;
import de.kaiserpfalzedv.ocp.groupsync.groups.OcpNameHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * @author rlichti {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2017-09-14
 */
public abstract class Action<T extends OcpNameHolder> {
    protected static final Logger LOG = LoggerFactory.getLogger(Action.class);

    protected T data;
    protected ExecuteRestCall sender;
    protected String localPart;


    protected Action(
            @NotNull final T data,
            @NotNull final String localPart,
            @NotNull final ExecuteRestCall sender
    ) {
        this.data = data;
        this.localPart = localPart;
        this.sender = sender;
    }

    abstract public Optional<String> execute() throws ExecuterException;

    public T getData() {
        return data;
    }
}
