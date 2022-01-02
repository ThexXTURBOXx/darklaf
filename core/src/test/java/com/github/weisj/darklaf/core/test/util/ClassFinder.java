/*
 * MIT License
 *
 * Copyright (c) 2019-2021 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.weisj.darklaf.core.test.util;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.weisj.darklaf.util.Lambdas;

public final class ClassFinder {

    @SuppressWarnings("unchecked")
    public static <T> List<Instantiable<T>> getInstancesOfType(final Class<T> type, final String... packages) {
        try (ResourceWalker walker = ResourceWalker.walkResources(packages)) {
            return walker.stream()
                    .filter(p -> p.endsWith(".class"))
                    .map(p -> p.replace('/', '.'))
                    .map(p -> p.substring(0, p.length() - 6))
                    .map(Lambdas.orDefault(Class::forName, null))
                    .filter(Objects::nonNull)
                    .filter(type::isAssignableFrom)
                    .filter(cls -> !cls.isInterface())
                    .filter(cls -> !Modifier.isAbstract(cls.getModifiers()))
                    .map(c -> new Instantiable<>((Class<T>) c))
                    .collect(Collectors.toList());
        }
    }

    public static <T> T getInstance(final Class<T> type) throws ReflectiveOperationException {
        return type.getDeclaredConstructor().newInstance();
    }
}
