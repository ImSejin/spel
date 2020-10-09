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

package io.github.imsejin.expression.spel.ast;

import io.github.imsejin.expression.EvaluationException;
import io.github.imsejin.expression.asm.Label;
import io.github.imsejin.expression.asm.MethodVisitor;
import io.github.imsejin.expression.spel.CodeFlow;
import io.github.imsejin.expression.spel.ExpressionState;
import io.github.imsejin.expression.spel.SpelEvaluationException;
import io.github.imsejin.expression.spel.SpelMessage;
import io.github.imsejin.expression.spel.support.BooleanTypedValue;

/**
 * Represents the boolean OR operation.
 *
 * @author Andy Clement
 * @author Mark Fisher
 * @author Oliver Becker
 * @since 3.0
 */
public class OpOr extends Operator {

    public OpOr(int startPos, int endPos, SpelNodeImpl... operands) {
        super("or", startPos, endPos, operands);
        this.exitTypeDescriptor = "Z";
    }

    @Override
    public BooleanTypedValue getValueInternal(ExpressionState state) throws EvaluationException {
        if (getBooleanValue(state, getLeftOperand())) {
            // no need to evaluate right operand
            return BooleanTypedValue.TRUE;
        }
        return BooleanTypedValue.forValue(getBooleanValue(state, getRightOperand()));
    }

    private boolean getBooleanValue(ExpressionState state, SpelNodeImpl operand) {
        try {
            Boolean value = operand.getValue(state, Boolean.class);
            assertValueNotNull(value);
            return value;
        } catch (SpelEvaluationException ee) {
            ee.setPosition(operand.getStartPosition());
            throw ee;
        }
    }

    private void assertValueNotNull(Boolean value) {
        if (value == null) {
            throw new SpelEvaluationException(SpelMessage.TYPE_CONVERSION_ERROR, "null", "boolean");
        }
    }

    @Override
    public boolean isCompilable() {
        SpelNodeImpl left = getLeftOperand();
        SpelNodeImpl right = getRightOperand();
        return (left.isCompilable() && right.isCompilable() &&
                CodeFlow.isBooleanCompatible(left.exitTypeDescriptor) &&
                CodeFlow.isBooleanCompatible(right.exitTypeDescriptor));
    }

    @Override
    public void generateCode(MethodVisitor mv, CodeFlow cf) {
        // pseudo: if (leftOperandValue) { result=true; } else { result=rightOperandValue; }
        Label elseTarget = new Label();
        Label endOfIf = new Label();
        cf.enterCompilationScope();
        getLeftOperand().generateCode(mv, cf);
        cf.unboxBooleanIfNecessary(mv);
        cf.exitCompilationScope();
        mv.visitJumpInsn(IFEQ, elseTarget);
        mv.visitLdcInsn(1); // TRUE
        mv.visitJumpInsn(GOTO, endOfIf);
        mv.visitLabel(elseTarget);
        cf.enterCompilationScope();
        getRightOperand().generateCode(mv, cf);
        cf.unboxBooleanIfNecessary(mv);
        cf.exitCompilationScope();
        mv.visitLabel(endOfIf);
        cf.pushDescriptor(this.exitTypeDescriptor);
    }

}
