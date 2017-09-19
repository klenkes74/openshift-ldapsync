package de.kaiserpfalzedv.ocp.groupsync;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import de.kaiserpfalzedv.ocp.groupsync.groups.Group;
import de.kaiserpfalzedv.ocp.groupsync.ldap.LdapGroupReader;
import de.kaiserpfalzedv.ocp.groupsync.ocp.OcpGroupReader;
import de.kaiserpfalzedv.ocp.groupsync.ocp.providers.ServerCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author rlichti {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2017-09-12
 */
@Service
public class SyncGroupsController implements Executor {
    private static final Logger LOG = LoggerFactory.getLogger(SyncGroupsController.class);


    @Value("${baseDN:-}")
    private String baseDN;

    @Inject
    private ServerCredentials serverCredentials;

    @Value("${ldapFilter:objectClass=*}")
    private String ldapFilter;

    @Inject
    private OcpGroupReader ocpGroupReader;

    @Inject
    private LdapGroupReader ldapGroupReader;

    @Inject
    private Set<GroupExecutor> groupExecutors = new HashSet<>();

    @PostConstruct
    public void init() {
        if ("-".equals(baseDN)) {
            baseDN = serverCredentials.getBaseDn();
        }
    }

    public void execute() throws ExecuterException {
        Map<String, Group> ocpGroups = ocpGroupReader.execute();
        Map<String, Group> ldapGroups = ldapGroupReader.load(baseDN, ldapFilter);

        for (GroupExecutor executor : groupExecutors) {
            executor.execute(ocpGroups, ldapGroups);
        }
    }
}
