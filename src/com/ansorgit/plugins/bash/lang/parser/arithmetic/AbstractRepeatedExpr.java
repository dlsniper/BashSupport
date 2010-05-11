/*
 * Copyright 2010 Joachim Ansorg, mail@ansorg-it.com
 * File: AbstractRepeatedExpr.java, Class: AbstractRepeatedExpr
 * Last modified: 2010-05-11
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ansorgit.plugins.bash.lang.parser.arithmetic;

import com.ansorgit.plugins.bash.lang.parser.BashPsiBuilder;
import com.ansorgit.plugins.bash.lang.parser.util.ParserUtil;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * Abstract class for a repeated expression which delegates the parsing of the subexpressions to
 * another parsing function.
 * <p/>
 * User: jansorg
 * Date: Feb 6, 2010
 * Time: 5:22:23 PM
 */
abstract class AbstractRepeatedExpr implements ArithmeticParsingFunction {
    private final ArithmeticParsingFunction next;
    private final TokenSet operators;
    private final IElementType partMarker;

    protected AbstractRepeatedExpr(ArithmeticParsingFunction next, TokenSet operators, IElementType partMarker) {
        this.next = next;
        this.operators = operators;
        this.partMarker = partMarker;
    }

    protected AbstractRepeatedExpr(ArithmeticParsingFunction next, TokenSet operators) {
        this(next, operators, null);
    }

    protected AbstractRepeatedExpr(ArithmeticParsingFunction next, IElementType operator) {
        this(next, TokenSet.create(operator), null);
    }

    protected AbstractRepeatedExpr(ArithmeticParsingFunction next, IElementType operator, IElementType marker) {
        this(next, TokenSet.create(operator), marker);
    }

    public boolean isValid(BashPsiBuilder builder) {
        return next.isValid(builder);
    }

    public boolean parse(BashPsiBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();

        int count = 0;
        boolean ok;

        //fixme fix with call to partialParsing
        do {
            ok = next.parse(builder);
            count++;
        } while (ok && ParserUtil.conditionalRead(builder, operators));

        if (ok && count > 1 && partMarker != null) {
            marker.done(partMarker);
        } else {
            marker.drop();
        }

        return ok;
    }

    public boolean isValidPartial(BashPsiBuilder builder) {
        return operators.contains(builder.getTokenType()) || next.isValidPartial(builder);
    }

    public boolean partialParsing(BashPsiBuilder builder) {
        boolean ok = operators.contains(builder.getTokenType());

        if (ok) {
            while (ok && ParserUtil.conditionalRead(builder, operators)) {
                ok = next.parse(builder);
            }
        } else {
            ok = next.partialParsing(builder);
        }

        return ok;
    }
}
