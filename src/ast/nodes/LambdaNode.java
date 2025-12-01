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
import ast.typesystem.types.FunType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * this is the node representing a lambda (fn x -> e).
 */
public final class LambdaNode extends SyntaxNode
{
    private Token variable;   // this is the formal parameter
    private SyntaxNode body;  // this is the function body

    public LambdaNode(Token variable, SyntaxNode body, long line)
    {
        super(line);
        this.variable = variable;
        this.body = body;
    }

    /**
     * this is where we evaluate a lambda by producing a closure.
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Environment capturedEnv = env.copy();
        return new Closure(variable, body, capturedEnv);
    }

    /**
     * this is where we compute the type (a -> b) for fn x -> body.
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        // this is a fresh type variable for the parameter
        VarType paramType = tenv.getTypeVariable();

        // this is the extended environment with x : paramType
        TypeEnvironment newTenv = tenv.copy();
        newTenv.updateEnvironment(variable, paramType);

        // this is the type of the body under that environment
        Type bodyType = body.typeOf(newTenv, inferencer);

        // this is where we apply substitutions to both pieces
        Type finalParamType = inferencer.getSubstitutions().apply(paramType);
        Type finalBodyType  = inferencer.getSubstitutions().apply(bodyType);

        // this is the function type a -> b
        return new FunType(finalParamType, finalBodyType);
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("Lambda(", indentAmt);
        printIndented("param: " + variable.getValue(), indentAmt + 2);
        printIndented("body:", indentAmt + 2);
        body.displaySubtree(indentAmt + 4);
        printIndented(")", indentAmt);
    }

    /**
     * this is the runtime closure for a lambda.
     */
    public static final class Closure
    {
        private final Token parameter;
        private final SyntaxNode body;
        private final Environment env;

        public Closure(Token parameter, SyntaxNode body, Environment env)
        {
            this.parameter = parameter;
            this.body = body;
            this.env = env;
        }

        public Token getParameter()   { return parameter; }
        public SyntaxNode getBody()   { return body; }
        public Environment getEnvironment() { return env; }

        @Override
        public String toString()
        {
            return "<closure " + parameter.getValue() + " -> ...>";
        }
    }
}
