package de.kaiserpfalzedv.ocp.groupsync;

import de.kaiserpfalzedv.ocp.groupsync.ExecuterException;
import de.kaiserpfalzedv.ocp.groupsync.groups.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author rlichti {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2017-09-12
 */
public interface GroupExecutor {
    void execute(Map<String, Group> ocpGroupNames, Map<String, Group> ldapGroups);
}
