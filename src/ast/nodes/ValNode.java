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
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * This node represents a value declaration:
 *   val x = expr
 */
public final class ValNode extends SyntaxNode
{
    // name being defined (identifier token)
    private Token name;

    // expression whose value we bind to name
    private SyntaxNode expr;

    /**
     * Construct a new ValNode.
     *
     * @param name  identifier token for the binding
     * @param expr  expression to evaluate and bind
     * @param line  source line number
     */
    public ValNode(Token name, SyntaxNode expr, long line)
    {
        super(line);
        this.name = name;
        this.expr = expr;
    }

    /**
     * Evaluate the val declaration:
     *   - evaluate expr
     *   - update the runtime environment with name = value
     *   - return the *name* of the binding (this is what the tests expect)
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
<<<<<<< HEAD
        Object val = expr.evaluate(env);

        // If this value is a function (closure), ensure its captured environment
        // also knows its own name so recursion works (fib, foo, revLst, etc.).
        if (val instanceof LambdaNode.Closure)
        {
            LambdaNode.Closure clo = (LambdaNode.Closure) val;
            clo.getEnvironment().updateEnvironment(name, clo);
        }

        if (env.lookup(name) == null)
            env.updateEnvironment(name, val);
        else 
        {
            logError(name.getValue() + " already defined.");
            throw new EvaluationException();
        }
=======
        // evaluate the right-hand side
        Object value = expr.evaluate(env);

        // update runtime environment
        env.updateEnvironment(name, value);

        // the result of `val x = ...` is the identifier "x", not the value
>>>>>>> 6a70306e82d2aa9f8199c52f0997b9db19103c69
        return name.getValue();
    }

    /**
     * Infer the type of the val declaration:
     *   - infer type of expr under current type environment
     *   - apply any current substitutions
     *   - bind name to that type in the type environment
     *   - the type of the whole declaration is the type of expr
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
<<<<<<< HEAD
        // If the expression is a lambda, we need to set up a placeholder
        if (expr instanceof LambdaNode) {
            // Create fresh α and β
            VarType t1 = tenv.getTypeVariable();
            VarType t2 = tenv.getTypeVariable();

            // Add name : t1 -> t2 to the environment BEFORE checking the body
            tenv.updateEnvironment(name, new FunType(t1, t2));
        }

        //  Now type-check the expression normally
        Type valType = expr.typeOf(tenv, inferencer);

        //  Apply substitutions (finalize)
        valType = inferencer.getSubstitutions().apply(valType);

        // Update environment with the final type (overwrites placeholder)
        tenv.updateEnvironment(name, valType);

        return valType;
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     * 
     * @param indentAmt the amout of indentation to perform.
     */
=======
        // get the type of the expression
        Type exprType = expr.typeOf(tenv, inferencer);

        // apply current substitutions
        Type finalType = inferencer.getSubstitutions().apply(exprType);

        // bind the name to its (possibly substituted) type
        tenv.updateEnvironment(name, finalType);

        // the type of the val declaration is the type of the expression
        return finalType;
    }

    @Override
>>>>>>> 6a70306e82d2aa9f8199c52f0997b9db19103c69
    public void displaySubtree(int indentAmt)
    {
        printIndented("ValNode(" + name.getValue() + ")", indentAmt);
        expr.displaySubtree(indentAmt + 2);
    }
}
