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
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.validation.constraints.NotNull;

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
public class LdapUserReader {
    private static final Logger LOG = LoggerFactory.getLogger(LdapUserReader.class);

    private LdapServer server;
    private UserConverter converter;

    @Inject
    public LdapUserReader setServer(
            @NotNull final LdapServer server,
            @NotNull final UserConverter converter
    ) {
        this.server = server;
        this.converter = converter;
        
        return this;
    }


    public Optional<User> load(final String dn) {
        try {
            Attributes entry = server.getByDn(dn);

            return converter.convert(dn, entry);
        } catch (NamingException e) {
            LOG.error(e.getClass().getSimpleName() + " caught: " + e.getMessage(), e);

            return Optional.empty();
        }
    }
}
