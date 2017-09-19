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

import java.util.HashSet;

import de.kaiserpfalzedv.ocp.groupsync.BuilderValidationException;
import org.apache.commons.lang3.builder.Builder;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author klenkes {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2017-09-10
 */
public class UserBuilder implements Builder<User> {
    private String dn;

    private String userName;

    private String fullName;

    private String email;


    @Override
    public User build() {
        validate();

        return new User(dn, userName, fullName, email);
    }

    private void validate() throws BuilderValidationException {
        HashSet<String> failures = new HashSet<>(3);

        if (isBlank(dn)) {
            failures.add("No DN in entry!");
        }

        if (isBlank(userName)) {
            failures.add("No userName in entry!");
        }

        if (! failures.isEmpty()) {
            throw new BuilderValidationException(User.class, failures);
        }
    }


    public UserBuilder withDn(String dn) {
        this.dn = dn;
        return this;
    }

    public UserBuilder withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public UserBuilder withFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
}
