/*
 *    Copyright 2017 Kaiserpfalz EDV-Service, Roland T. Lichti
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.kaiserpfalzedv.ocp.groupsync.ldap;

import java.util.*;

import javax.inject.Inject;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import javax.validation.constraints.NotNull;

import de.kaiserpfalzedv.ocp.groupsync.ExecuterException;
import de.kaiserpfalzedv.ocp.groupsync.groups.Group;
import de.kaiserpfalzedv.ocp.groupsync.groups.GroupConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author klenkes {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2017-09-10
 */
@Service
public class LdapGroupReader {
    private static final Logger LOG = LoggerFactory.getLogger(LdapGroupReader.class);

    private LdapServer server;
    private GroupConverter converter;


    @Inject
    public LdapGroupReader(
            @NotNull final LdapServer server,
            @NotNull final GroupConverter converter
    ) {
        this.server = server;
        this.converter = converter;
    }


    public Map<String, Group> load(final String baseDN, final String filter) {
        HashMap<String, Group> result = new HashMap<>();

        NamingEnumeration<SearchResult> ldapGroups;
        try {
            ldapGroups = server.search(baseDN, filter);
        } catch (NamingException e) {
            LOG.error("Can't search for groups: baseDN={}, filter={}", baseDN, filter);

            throw new ExecuterException("Can't search for groups: baseDN=" + baseDN + ", filter="+ filter);
        }

        while (ldapGroups != null && ldapGroups.hasMoreElements()) {
            SearchResult ldapGroup = ldapGroups.nextElement();

            try {
                LOG.trace("Working on: name={}, nameInNamespace={}, attributes={}", ldapGroup.getName(), ldapGroup.getNameInNamespace(), ldapGroup.getAttributes().getIDs());
                Optional<Group> group = converter.convert(ldapGroup.getName(), ldapGroup.getAttributes());

                group.ifPresent(g -> result.put(g.getOcpName(), g));
            } catch (NamingException e) {
                LOG.error("Can't read all users into group: group={}", ldapGroup.getName());

                throw new ExecuterException("Can't read all users into group: " + ldapGroup.getName());
            }
        }

        result.remove("Groups"); // removing master group.

        LOG.info("LDAP groups: {}", result.keySet());
        return result;
    }
}
