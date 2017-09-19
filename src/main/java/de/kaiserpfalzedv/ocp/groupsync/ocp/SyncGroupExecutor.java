package de.kaiserpfalzedv.ocp.groupsync.ocp;

import de.kaiserpfalzedv.ocp.groupsync.GroupExecutor;
import de.kaiserpfalzedv.ocp.groupsync.groups.Group;
import de.kaiserpfalzedv.ocp.groupsync.groups.GroupBuilder;
import de.kaiserpfalzedv.ocp.groupsync.ocp.actions.*;
import de.kaiserpfalzedv.ocp.groupsync.templating.YamlPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author rlichti {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2017-09-12
 */
@Service
public class SyncGroupExecutor implements GroupExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(SyncGroupExecutor.class);

    @Inject
    private ExecuteRestCall sender;

    @Value("${groupTemplate:GroupTemplate.ftl}")
    private String groupTemplate;

    @Inject
    private YamlPrinter printer;


    public void execute(Map<String, Group> ocpGroups, Map<String, Group> ldapGroups) {

        HashSet<String> syncGroups = createSynchronizationSelector(ocpGroups, ldapGroups);
        HashSet<String> deletionGroups = createSelector(ldapGroups, ocpGroups);
        HashSet<String> creationGroups = createSelector(ocpGroups, ldapGroups);

        HashSet<Action<Group>> actions = new HashSet<>();
        actions.addAll(prepareDeletionGroups(ocpGroups, deletionGroups));
        actions.addAll(prepareSyncGroups(ocpGroups, ldapGroups, syncGroups));
        actions.addAll(prepareCreationGroups(ldapGroups, creationGroups));

        actions.forEach(Action::execute);
    }

    private HashSet<String> createSynchronizationSelector(Map<String, Group> ocpGroups, Map<String, Group> ldapGroups) {
        HashSet<String> syncGroups = new HashSet<>();
        for(String g : ocpGroups.keySet()) {
            if (ldapGroups.containsKey(g)) {
                syncGroups.add(g);
            }
        }
        return syncGroups;
    }

    private HashSet<String> createSelector(Map<String, Group> ocpGroups, Map<String, Group> ldapGroups) {
        HashSet<String> creationGroups = new HashSet<>();
        for(String g : ldapGroups.keySet()) {
            if (! ocpGroups.containsKey(g)) {
                creationGroups.add(g);
            }
        }
        return creationGroups;
    }

    private Set<Delete<Group>> prepareDeletionGroups(final Map<String, Group> groups, final Set<String> selector) {
        HashSet<Delete<Group>> result = new HashSet<>(selector.size());

        for (String groupName : selector) {
            LOG.trace("Preparing group for deletion: {}", groupName);

            Delete<Group> action = new Delete<>(groups.get(groupName), Group.LOCAL_PART, sender);
            result.add(action);
        }

        LOG.info("Prepared {} groups to delete: {}", selector.size(), selector);
        return result;
    }


    private Set<Update<Group>> prepareSyncGroups(final Map<String, Group> ocpGroups, final Map<String, Group> ldapGroups, final Set<String> selector) {
        HashSet<Update<Group>> result = new HashSet<>(selector.size());

        for (String groupName : selector) {
            LOG.trace("Preparing group for synchronization: {}", groupName);

            Group ocp = ocpGroups.get(groupName);
            Group ldap = ldapGroups.get(groupName);

            Group data = new GroupBuilder()
                    .withDn(ldap.getDn())
                    .withLdapServer(ldap.getLdapServer())
                    .withOcpName(ocp.getOcpName())
                    .withUuid(ocp.getUuid())
                    .withResourceVersion(ocp.getResourceVersion())
                    .addUsers(ldap.getUsers())
                    .addGroups(ldap.getGroups())
                    .build();

            Update<Group> action = new Update<>(data, Group.LOCAL_PART, sender, printer, groupTemplate);
            result.add(action);
        }

        LOG.info("Prepared {} groups to synchronize: {}", selector.size(), selector);
        return result;
    }


    private Set<Create<Group>> prepareCreationGroups(final Map<String, Group> groups, final Set<String> selector) {
        HashSet<Create<Group>> result = new HashSet<>(selector.size());

        for (String groupName : selector) {
            LOG.trace("Preparing group for creation: {}", groupName);

            Create<Group> action = new Create<>(groups.get(groupName), Group.LOCAL_PART, sender, printer, groupTemplate);
            result.add(action);
        }

        LOG.info("Prepared {} groups to create: {}", selector.size(), selector);
        return result;
    }
}