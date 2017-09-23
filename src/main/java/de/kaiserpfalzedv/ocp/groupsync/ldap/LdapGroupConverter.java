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

import java.util.Optional;

import javax.inject.Inject;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.validation.constraints.NotNull;

import de.kaiserpfalzedv.ocp.groupsync.groups.Group;
import de.kaiserpfalzedv.ocp.groupsync.groups.GroupBuilder;
import de.kaiserpfalzedv.ocp.groupsync.groups.GroupConverter;
import de.kaiserpfalzedv.ocp.groupsync.groups.User;
import de.kaiserpfalzedv.ocp.groupsync.groups.UserConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author klenkes {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2017-09-10
 */
@Service
public class LdapGroupConverter implements GroupConverter {
    private static final Logger LOG = LoggerFactory.getLogger(LdapGroupConverter.class);

    private static final String OCP_GROUP_NAME_ATTRIBUTE = "cn";
    private static final String MEMBER_ATTRIBUTE = "member";

    private LdapServer server;
    private UserConverter userConverter;


    @Inject
    public LdapGroupConverter(
            @NotNull final LdapServer server,
            @NotNull final UserConverter userConverter
    ) {
        this.server = server;
        this.userConverter = userConverter;
    }

    @Override
    public Optional<Group> convert(final String dn, final Attributes ldapGroup) throws NamingException {
        LOG.info("Working on LDAP group: {}", dn);
        GroupBuilder builder = new GroupBuilder()
                .withDn(dn)
                .withLdapServer(server.getName())
                .withOcpName(calculateOCPGroupName(ldapGroup));


        try {
            @SuppressWarnings("unchecked")
            NamingEnumeration<String> members = (NamingEnumeration<String>) ldapGroup.get(MEMBER_ATTRIBUTE).getAll();
            LOG.info("Group has members: group={}, members={}", dn, members);

            while (members.hasMoreElements()) {
                String memberDn = members.nextElement();
                LOG.debug("Working on member: {}", memberDn);

                Attributes memberEntry = server.getByDn(memberDn);

                if (isUser(memberEntry)) {
                    LOG.trace("Member is an user: dn={}", memberDn);
                    Optional<User> memberUser = userConverter.convert(memberDn, memberEntry);
                    memberUser.ifPresent(builder::addUser);
                } else { /* need to load the new group data ... */
                    LOG.trace("Member is a group: dn={}", memberDn);
                    Optional<Group> memberGroup = convert(memberDn, memberEntry);
                    memberGroup.ifPresent(builder::addGroup);
                }
            }
        } catch (NullPointerException e) {
            LOG.debug("LDAP Group has no members: {}", dn);
        }

        return Optional.of(builder.build());
    }


    private String calculateOCPGroupName(final Attributes entry) {
        return convert(OCP_GROUP_NAME_ATTRIBUTE, entry.get(OCP_GROUP_NAME_ATTRIBUTE));
    }


    private String convert(final String attributeName, final Attribute ldapAttribute) {
        try {
            return (String) ldapAttribute.get(0);
        } catch (NullPointerException | NamingException e) {
            throw new IllegalArgumentException("No valid string attribute: " + attributeName + "=" + ldapAttribute);
        }
    }

    private boolean isGroup(final Attributes entry) {
        return entry.get("objectClass").contains("groupOfNames");
    }

    private boolean isUser(final Attributes entry) {
        return !isGroup(entry);
    }
}
