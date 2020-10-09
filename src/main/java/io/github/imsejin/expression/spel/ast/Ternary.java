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

import io.github.imsejin.expression.asm.Label;
import io.github.imsejin.expression.asm.MethodVisitor;
import io.github.imsejin.expression.EvaluationException;
import io.github.imsejin.expression.TypedValue;
import io.github.imsejin.expression.spel.CodeFlow;
import io.github.imsejin.expression.spel.ExpressionState;
import io.github.imsejin.expression.spel.SpelEvaluationException;
import io.github.imsejin.expression.spel.SpelMessage;
import io.github.imsejin.expression.util.Assert;
import io.github.imsejin.expression.util.ObjectUtils;

/**
 * Represents a ternary expression, for example: "someCheck()?true:false".
 *
 * @author Andy Clement
 * @author Juergen Hoeller
 * @since 3.0
 */
public class Ternary extends SpelNodeImpl {

	public Ternary(int startPos, int endPos, SpelNodeImpl... args) {
		super(startPos, endPos, args);
	}


	/**
	 * Evaluate the condition and if true evaluate the first alternative, otherwise
	 * evaluate the second alternative.
	 * @param state the expression state
	 * @throws EvaluationException if the condition does not evaluate correctly to
	 * a boolean or there is a problem executing the chosen alternative
	 */
	@Override
	public TypedValue getValueInternal(ExpressionState state) throws EvaluationException {
		Boolean value = this.children[0].getValue(state, Boolean.class);
		if (value == null) {
			throw new SpelEvaluationException(getChild(0).getStartPosition(),
					SpelMessage.TYPE_CONVERSION_ERROR, "null", "boolean");
		}
		TypedValue result = this.children[value ? 1 : 2].getValueInternal(state);
		computeExitTypeDescriptor();
		return result;
	}

	@Override
	public String toStringAST() {
		return getChild(0).toStringAST() + " ? " + getChild(1).toStringAST() + " : " + getChild(2).toStringAST();
	}

	private void computeExitTypeDescriptor() {
		if (this.exitTypeDescriptor == null && this.children[1].exitTypeDescriptor != null &&
				this.children[2].exitTypeDescriptor != null) {
			String leftDescriptor = this.children[1].exitTypeDescriptor;
			String rightDescriptor = this.children[2].exitTypeDescriptor;
			if (ObjectUtils.nullSafeEquals(leftDescriptor, rightDescriptor)) {
				this.exitTypeDescriptor = leftDescriptor;
			}
			else {
				// Use the easiest to compute common super type
				this.exitTypeDescriptor = "Ljava/lang/Object";
			}
		}
	}

	@Override
	public boolean isCompilable() {
		SpelNodeImpl condition = this.children[0];
		SpelNodeImpl left = this.children[1];
		SpelNodeImpl right = this.children[2];
		return (condition.isCompilable() && left.isCompilable() && right.isCompilable() &&
				CodeFlow.isBooleanCompatible(condition.exitTypeDescriptor) &&
				left.exitTypeDescriptor != null && right.exitTypeDescriptor != null);
	}

	@Override
	public void generateCode(MethodVisitor mv, CodeFlow cf) {
		// May reach here without it computed if all elements are literals
		computeExitTypeDescriptor();
		cf.enterCompilationScope();
		this.children[0].generateCode(mv, cf);
		String lastDesc = cf.lastDescriptor();
		Assert.state(lastDesc != null, "No last descriptor");
		if (!CodeFlow.isPrimitive(lastDesc)) {
			CodeFlow.insertUnboxInsns(mv, 'Z', lastDesc);
		}
		cf.exitCompilationScope();
		Label elseTarget = new Label();
		Label endOfIf = new Label();
		mv.visitJumpInsn(IFEQ, elseTarget);
		cf.enterCompilationScope();
		this.children[1].generateCode(mv, cf);
		if (!CodeFlow.isPrimitive(this.exitTypeDescriptor)) {
			lastDesc = cf.lastDescriptor();
			Assert.state(lastDesc != null, "No last descriptor");
			CodeFlow.insertBoxIfNecessary(mv, lastDesc.charAt(0));
		}
		cf.exitCompilationScope();
		mv.visitJumpInsn(GOTO, endOfIf);
		mv.visitLabel(elseTarget);
		cf.enterCompilationScope();
		this.children[2].generateCode(mv, cf);
		if (!CodeFlow.isPrimitive(this.exitTypeDescriptor)) {
			lastDesc = cf.lastDescriptor();
			Assert.state(lastDesc != null, "No last descriptor");
			CodeFlow.insertBoxIfNecessary(mv, lastDesc.charAt(0));
		}
		cf.exitCompilationScope();
		mv.visitLabel(endOfIf);
		cf.pushDescriptor(this.exitTypeDescriptor);
	}

}
