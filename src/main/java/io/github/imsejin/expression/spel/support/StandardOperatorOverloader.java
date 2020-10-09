/*
 * Copyright 2002-2018 the original author or authors.
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

package io.github.imsejin.expression.spel.support;

import io.github.imsejin.expression.EvaluationException;
import io.github.imsejin.expression.Operation;
import io.github.imsejin.expression.OperatorOverloader;

/**
 * Standard implementation of {@link OperatorOverloader}.
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public class StandardOperatorOverloader implements OperatorOverloader {

    @Override
    public boolean overridesOperation(Operation operation, Object leftOperand, Object rightOperand)
            throws EvaluationException {

        return false;
    }

    @Override
    public Object operate(Operation operation, Object leftOperand, Object rightOperand)
            throws EvaluationException {

        throw new EvaluationException("No operation overloaded by default");
    }

}
