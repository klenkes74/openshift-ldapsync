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

package de.kaiserpfalzedv.ocp.groupsync.groups;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import de.kaiserpfalzedv.ocp.groupsync.BuilderValidationException;
import org.apache.commons.lang3.builder.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author klenkes {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2017-09-10
 */
public class GroupBuilder implements Builder<Group> {
    private String ldapServer;
    
    private String dn;

    private String ocpName;

    private OffsetDateTime syncDate;

    private String uuid;

    private String resourceVersion;

    private final HashSet<User> users = new HashSet<>();

    private final HashSet<Group> groups = new HashSet<>();

    
    @Override
    public Group build() {
        validate();

        Group result = new Group(ldapServer, dn, ocpName, syncDate);

        result.addUsers(users);
        result.addGroups(groups);

        if (uuid != null) {
            result.setUuid(uuid);
        }

        if (resourceVersion != null) {
            result.setResourceVersion(resourceVersion);
        }

        return result;
    }

    private void validate() throws BuilderValidationException {
        HashSet<String> failures = new HashSet<>(3);

        if (syncDate == null) {
            syncDate = OffsetDateTime.now(ZoneId.of("UTC"));
        }

        if (isBlank(ldapServer)) {
            failures.add("No ldap server in entry!");
        }

        if (isBlank(dn)) {
            failures.add("No DN in entry!");
        }

        if (isBlank(ocpName)) {
            failures.add("No OCP group name given!");
        }

        if (! failures.isEmpty()) {
            throw new BuilderValidationException(User.class, failures);
        }
    }


    public GroupBuilder withLdapServer(final String ldapServer) {
        this.ldapServer = ldapServer;
        return this;
    }

    public GroupBuilder withSyncDate(final OffsetDateTime syncDate) {
        this.syncDate = syncDate;
        return this;
    }

    public GroupBuilder withDn(final String dn) {
        this.dn = dn;
        return this;
    }

    public GroupBuilder withOcpName(final String ocpName) {
        this.ocpName = ocpName;
        return this;
    }

    public GroupBuilder withResourceVersion(@NotNull  final String resourceVersion) {
        this.resourceVersion = resourceVersion;
        return this;
    }

    public GroupBuilder withUuid(@NotNull final String uuid) {
        this.uuid = uuid;
        return this;
    }


    public GroupBuilder addUser(final User user) {
        this.users.add(user);
        return this;
    }
    
    public GroupBuilder removeUser(final User user) {
        this.users.remove(user);
        return this;
    }

    public GroupBuilder addUsers(final Collection<User> users) {
        this.users.addAll(users);
        return this;
    }
    
    public GroupBuilder removeUsers(final Collection<User> users) {
        this.users.removeAll(users);
        return this;
    }


    public GroupBuilder addGroup(final Group Group) {
        this.groups.add(Group);
        return this;
    }

    public GroupBuilder removeGroup(final Group Group) {
        this.groups.remove(Group);
        return this;
    }

    public GroupBuilder addGroups(final Collection<Group> Groups) {
        this.groups.addAll(Groups);
        return this;
    }

    public GroupBuilder removeGroups(final Collection<Group> Groups) {
        this.groups.removeAll(Groups);
        return this;
    }
}
