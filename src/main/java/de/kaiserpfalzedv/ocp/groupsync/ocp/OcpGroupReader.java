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

package de.kaiserpfalzedv.ocp.groupsync.ocp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.kaiserpfalzedv.ocp.groupsync.ExecuterException;
import de.kaiserpfalzedv.ocp.groupsync.groups.Group;
import de.kaiserpfalzedv.ocp.groupsync.groups.GroupBuilder;
import de.kaiserpfalzedv.ocp.groupsync.groups.User;
import de.kaiserpfalzedv.ocp.groupsync.groups.UserBuilder;
import de.kaiserpfalzedv.ocp.groupsync.ocp.actions.ExecuteRestCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author klenkes {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2017-09-13
 */
@Service
public class OcpGroupReader {
    private static final Logger LOG = LoggerFactory.getLogger(OcpGroupReader.class);

    @Value("${ocpServer:-}")
    private String masterUrl;

    @Inject
    private ExecuteRestCall sender;

    public Map<String, Group> execute() throws ExecuterException {
        HashMap<String, Group> result = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode items;
        try {
            Optional<String> ocpBody = sender.get(Group.LOCAL_PART);
            if (! ocpBody.isPresent()) {
                throw new ExecuterException("Can't retrieve groups from OCP master servers!");
            }

            items = mapper.readTree(ocpBody.get()).get("items");
        } catch (IOException e) {
            throw new ExecuterException("Can't read groups: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
        }

        items.elements().forEachRemaining(i -> {
            GroupBuilder builder = new GroupBuilder();

            JsonNode metadata = i.get("metadata");
            JsonNode annotations = metadata.get("annotations");
            JsonNode ocpUsers = i.get("users");

            builder.withOcpName(metadata.get("name").asText());

            try {
                builder.withResourceVersion(metadata.get("resourceVersion").asText());
            } catch (NullPointerException e) {
                // nothing to do. No resourceVersion is required!
            }

            try {
                builder.withUuid(metadata.get("uid").asText());
            } catch (NullPointerException e) {
                // nothing to do. No UID required.
            }

            try {
                builder.withDn(annotations.get("ldap-group").asText());
            } catch (NullPointerException e) {
                builder.withDn("-");
            }
            try {
                builder.withLdapServer(annotations.get("ldap-server").asText());
            } catch (NullPointerException e) {
                builder.withLdapServer("-");
            }
            try {
                OffsetDateTime syncDate = OffsetDateTime.parse(annotations.get("ldap-sync").asText());
                builder.withSyncDate(syncDate);
            } catch (NullPointerException | DateTimeParseException e) {
                // no sync time given. Will be replaced with NOW by the builder.
            }

            HashSet<User> users = new HashSet<>();
            ocpUsers.elements().forEachRemaining(u -> {
                User user = new UserBuilder()
                        .withUserName(u.asText())
                        .withDn(u.asText())
                        .build();
                users.add(user);
            });

            builder.addUsers(users);
            Group group = builder.build();
            result.put(group.getOcpName(), group);
        });

        LOG.info("OCP groups: {}", result.keySet());
        return result;
    }
}
