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

package de.kaiserpfalzedv.ocp.groupsync;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author klenkes {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2017-09-10
 */
public class BuilderValidationException extends IllegalStateException implements Serializable {

    private Class<?> clasz;
    private final HashSet<String> failures = new HashSet<>();

    public BuilderValidationException(final Class<?> clasz, final Collection<String> failures) {
        super(failureMessage(clasz, failures));

        this.clasz = clasz;
        this.failures.addAll(failures);
    }

    private static String failureMessage(final Class<?> clasz, final Collection<String> failures) {
        StringBuilder result = new StringBuilder("Could not create ")
                .append(clasz.getSimpleName())
                .append(": ");

        for(String failure : failures) {
            result.append("\n- ").append(failure);
        }

        return result.toString();
    }


    public Class<?> getClasz() {
        return clasz;
    }

    public HashSet<String> getFailures() {
        return failures;
    }
}
