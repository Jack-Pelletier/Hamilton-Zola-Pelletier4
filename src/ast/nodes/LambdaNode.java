package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * This node represents a lambda (λ) expression.
 */
public final class LambdaNode extends SyntaxNode
{
    private Token variable;        // The formal parameter.
    private SyntaxNode body;       // The function body.

    /**
     * Constructs a new lambda node.
     *
     * @param variable the parameter of the lambda.
     * @param body     the body of the lambda.
     * @param line     the line of code the node is associated with.
     */
    public LambdaNode(Token variable, SyntaxNode body, long line)
    {
        super(line);
        this.variable = variable;
        this.body = body;
    }

    /**
     * Evaluate the node.
     *
     * @param env the executional environment we should evaluate the node under.
     * @return a Closure object capturing the parameter, body, and environment.
     * @throws EvaluationException if the evaluation fails.
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        // Capture a copy of the current environment in the closure.
        Environment capturedEnv = env.copy();
        return new Closure(variable, body, capturedEnv);
    }

    /**
     * Determine the type of the syntax node. In particular bool, int, real,
     * generic, or function.
     *
     * For a lambda, we construct a function type from the parameter type to the
     * body type. The parameter type is a fresh type variable in the type
     * environment.
     *
     * @param tenv       the type environment.
     * @param inferencer the type inferencer.
     * @return The type of the syntax node.
     * @throws TypeException if there is a type error.
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        // Create a fresh type variable for the parameter.
        VarType paramType = tenv.getTypeVariable();

        // Work with a copy of the current type environment.
        TypeEnvironment newTenv = tenv.copy();
        newTenv.updateEnvironment(variable, paramType);

        // Compute the type of the body under the extended environment.
        Type bodyType = body.typeOf(newTenv, inferencer);

        // Apply any accumulated substitutions to both parameter and body types.
        Type finalParamType = inferencer.getSubstitutions().apply(paramType);
        Type finalBodyType  = inferencer.getSubstitutions().apply(bodyType);

        // Return a function type paramType -> bodyType.
        return new FunType(finalParamType, finalBodyType);
    }

    /**
     * Display an AST subtree with the indentation specified.
     *
     * @param indentAmt the amount of indentation to perform.
     */
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
     * Represents a runtime closure for a lambda expression.
     */
    public static final class Closure
    {
        private final Token parameter;       // The formal parameter.
        private final SyntaxNode body;       // The body of the function.
        private final Environment env;       // The captured environment.

        /**
         * Construct a new closure.
         *
         * @param parameter the formal parameter.
         * @param body      the body of the function.
         * @param env       the captured environment.
         */
        public Closure(Token parameter, SyntaxNode body, Environment env)
        {
            this.parameter = parameter;
            this.body = body;
            this.env = env;
        }

        public Token getParameter()
        {
            return parameter;
        }

        public SyntaxNode getBody()
        {
            return body;
        }

        public Environment getEnvironment()
        {
            return env;
        }

        @Override
        public String toString()
        {
            return "<closure " + parameter.getValue() + " -> ... >";
        }
    }

    /**
     * Represents a function type from one type to another.
     */
    public static final class FunType extends Type
    {
        private final Type paramType;   // The parameter type.
        private final Type returnType;  // The return type.

        /**
         * Construct a new function type.
         *
         * @param paramType  the parameter type.
         * @param returnType the return type.
         */
        public FunType(Type paramType, Type returnType)
        {
            this.paramType = paramType;
            this.returnType = returnType;
        }

        /**
         * Get the parameter type.
         *
         * @return the parameter type.
         */
        public Type getParamType()
        {
            return paramType;
        }

        /**
         * Get the return type.
         *
         * @return the return type.
         */
        public Type getReturnType()
        {
            return returnType;
        }

        /**
         * Check equality of function types.
         *
         * @param obj the object to test.
         */
        @Override
        public boolean equals(Object obj)
        {
            // Check to see if we are comparing to ourself.
            if (obj == this)
                return true;

            if (!(obj instanceof FunType))
                return false;

            FunType rhs = (FunType) obj;

            return paramType.equals(rhs.paramType)
                    && returnType.equals(rhs.returnType);
        }

        /**
         * Gets the type as a string.
         *
         * @return the type as a string.
         */
        @Override
        public String toString()
        {
            return "(" + paramType.toString() + " -> " + returnType.toString()
                    + ")";
        }
    }
}