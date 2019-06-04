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

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import de.kaiserpfalzedv.ocp.groupsync.groups.User;
import de.kaiserpfalzedv.ocp.groupsync.groups.UserBuilder;
import de.kaiserpfalzedv.ocp.groupsync.groups.UserConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author klenkes {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2017-09-10
 */
@Service
public class LdapUserConverter implements UserConverter {
    private static final Logger LOG = LoggerFactory.getLogger(LdapUserConverter.class);

    @Value("${ldapUserNameAttribute:mail}")
    private String userNameAttribute;

    @Value("${ldapFullNameAttribute:cn}")
    private String fullNameAttribute;

    @Value("${ldapEmailAttribute:mail}")
    private String emailAttribute;

    @Override
    public Optional<User> convert(final String dn, final Attributes entry) {
        try {
            UserBuilder result = new UserBuilder()
                    .withDn(dn)
                    .withUserName(calculateUserName(entry));

            try {
                result.withFullName(convert(fullNameAttribute, entry.get(fullNameAttribute)));
            } catch (IllegalArgumentException e) {
                LOG.warn("User does not have valid full name: dn={}, missing attribute={}", dn, fullNameAttribute);
            }
            try {
                result.withEmail(convert(emailAttribute, entry.get(emailAttribute)));
            } catch (IllegalArgumentException e) {
                LOG.warn("User does not have valid email: dn={}, missing attribute={}", dn, emailAttribute);
            }


            if (LOG.isDebugEnabled()) {
                LOG.debug("Converted user: dn={}, user={}", dn, result.build());
            }
            return Optional.of(result.build());
        } catch (IllegalStateException | IllegalArgumentException e) {
            LOG.error("Could not convert user from AD: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String calculateUserName(Attributes entry) {
        String ldapName = convert(userNameAttribute, entry.get(userNameAttribute));
        String result = ldapName.toLowerCase();

        LOG.trace("Login generated for: ldapName={}, login={}", ldapName, result);
        return result;
    }


    private String convert(final String attributeName, final Attribute ldapAttribute) {
        try {
            return (String) ldapAttribute.get(0);
        } catch (NullPointerException | NamingException e) {
            LOG.warn("User entry does not include attribute: {}", attributeName);

            throw new IllegalArgumentException("No valid string attribute: " + attributeName);
        }
    }
}
