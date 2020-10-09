/*
 * Copyright 2002-2017 the original author or authors.
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

package io.github.imsejin.expression.core.convert.support;

import io.github.imsejin.expression.core.convert.ConversionService;
import io.github.imsejin.expression.core.convert.TypeDescriptor;
import io.github.imsejin.expression.util.Assert;

import java.beans.PropertyEditorSupport;

/**
 * Adapter that exposes a {@link java.beans.PropertyEditor} for any given
 * {@link ConversionService} and specific target type.
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public class ConvertingPropertyEditorAdapter extends PropertyEditorSupport {

    private final ConversionService conversionService;

    private final TypeDescriptor targetDescriptor;

    private final boolean canConvertToString;

    /**
     * Create a new ConvertingPropertyEditorAdapter for a given
     * {@link ConversionService}
     * and the given target type.
     *
     * @param conversionService the ConversionService to delegate to
     * @param targetDescriptor  the target type to convert to
     */
    public ConvertingPropertyEditorAdapter(ConversionService conversionService, TypeDescriptor targetDescriptor) {
        Assert.notNull(conversionService, "ConversionService must not be null");
        Assert.notNull(targetDescriptor, "TypeDescriptor must not be null");
        this.conversionService = conversionService;
        this.targetDescriptor = targetDescriptor;
        this.canConvertToString = conversionService.canConvert(this.targetDescriptor, TypeDescriptor.valueOf(String.class));
    }

    @Override
    public String getAsText() {
        if (this.canConvertToString) {
            return (String) this.conversionService.convert(getValue(), this.targetDescriptor, TypeDescriptor.valueOf(String.class));
        } else {
            return null;
        }
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(this.conversionService.convert(text, TypeDescriptor.valueOf(String.class), this.targetDescriptor));
    }

}