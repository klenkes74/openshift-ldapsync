package de.kaiserpfalzedv.ocp.groupsync.ocp.actions;

import de.kaiserpfalzedv.ocp.groupsync.ExecuterException;
import de.kaiserpfalzedv.ocp.groupsync.groups.OcpNameHolder;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * @author rlichti {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2017-09-14
 */
public class Retrieve<T extends OcpNameHolder> extends Action<T> {

    public Retrieve(
            @NotNull T data,
            @NotNull String localPart,
            @NotNull ExecuteRestCall sender
    ) {
        super(data, localPart, sender);
    }

    @Override
    public Optional<String> execute() throws ExecuterException {
        LOG.info("Retrieving: {}", data.getOcpName());

        return sender.get(localPart + data.getOcpName());
    }
}
