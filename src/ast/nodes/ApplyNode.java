/*
 *   Copyright (C) 2022 -- 2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ast.nodes;

import ast.EvaluationException;
import ast.nodes.LambdaNode.Closure;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.FunType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Represents a function application: (E1)(E2)
 */
public final class ApplyNode extends SyntaxNode
{
    private final SyntaxNode function;
    private final SyntaxNode argument;

    public ApplyNode(SyntaxNode function, SyntaxNode argument, long line)
    {
        super(line);
        this.function = function;
        this.argument = argument;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object f = function.evaluate(env);

        if (!(f instanceof Closure))
            throw new EvaluationException();

        Closure closure = (Closure) f;

        // Evaluate argument
        Object argVal = argument.evaluate(env);

        // New env from closure
        Environment newEnv = closure.getEnvironment().copy();
        newEnv.updateEnvironment(closure.getParameter(), argVal);

        // Evaluate body in closure environment
        return closure.getBody().evaluate(newEnv);
    }

    /**
     * Typing rule:
     *   function : argType -> resultType
     *   argument : argType
     *   --------------------------------
     *   (function)(argument) : resultType
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type funType = function.typeOf(tenv, inferencer);
        Type argType = argument.typeOf(tenv, inferencer);

        // fresh result type α
        VarType resultType = tenv.getTypeVariable();

        // Expected: argType -> α
        FunType expected = new FunType(argType, resultType);

        inferencer.unify(funType, expected,
                buildErrorMessage("function application has incompatible types."));

        // Return unified result type
        return inferencer.getSubstitutions().apply(resultType);
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("ApplyNode(", indentAmt);
        printIndented("function:", indentAmt + 2);
        function.displaySubtree(indentAmt + 4);
        printIndented("argument:", indentAmt + 2);
        argument.displaySubtree(indentAmt + 4);
        printIndented(")", indentAmt);
    }
}
