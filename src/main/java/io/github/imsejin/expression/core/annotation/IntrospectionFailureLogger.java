/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.imsejin.expression.core.annotation;

/**
 * Log facade used to handle annotation introspection failures (in particular
 * {@code TypeNotPresentExceptions}). Allows annotation processing to continue,
 * assuming that when Class attribute values are not resolvable the annotation
 * should effectively disappear.
 *
 * @author Phillip Webb
 * @since 5.2
 */
enum IntrospectionFailureLogger {

    DEBUG {
        @Override
        public boolean isEnabled() {
            return true;
        }
    },

    INFO {
        @Override
        public boolean isEnabled() {
            return true;
        }
    };

    void log(String message) {
        System.out.println(message);
    }

    void log(String message, Object source, Exception ex) {
        String on = (source != null ? " on " + source : "");
        System.out.println(message + on + ": " + ex);
    }

    abstract boolean isEnabled();

}
