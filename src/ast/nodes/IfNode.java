package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.BoolType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * This node represents an if-then-else expression.
 */
public final class IfNode extends SyntaxNode
{
    private final SyntaxNode cond;
    private final SyntaxNode thenBranch;
    private final SyntaxNode elseBranch;

    /**
     * Constructs a new if node.
     *
     * @param cond       the condition expression.
     * @param thenBranch the then branch expression.
     * @param elseBranch the else branch expression.
     * @param line       the line of code the node is associated with.
     */
    public IfNode(SyntaxNode cond, SyntaxNode thenBranch,
                  SyntaxNode elseBranch, long line)
    {
        super(line);
        this.cond       = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    /**
     * Evaluate the node.
     *
     * @param env the executional environment we should evaluate the node under.
     * @return the object representing the result of the evaluation.
     * @throws EvaluationException if the evaluation fails.
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object cval = cond.evaluate(env);
        if (!(cval instanceof Boolean))
        {
            logError("if condition must be boolean.");
            throw new EvaluationException();
        }

        if ((Boolean) cval)
            return thenBranch.evaluate(env);
        else
            return elseBranch.evaluate(env);
    }

    /**
     * Determine the type of the syntax node.
     *
     * @param tenv       the type environment.
     * @param inferencer the type inferencer
     * @return The type of the syntax node.
     * @throws TypeException if there is a type error.
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        // Get the type of the condition.
        Type condTy = cond.typeOf(tenv, inferencer);
        condTy = inferencer.getSubstitutions().apply(condTy);

        // Condition must be bool.
        if (!condTy.equals(new BoolType()))
        {
            throw new TypeException(
                    buildErrorMessage("if condition must be bool."));
        }

        // Get the types of the branches.
        Type thenTy = thenBranch.typeOf(tenv, inferencer);
        Type elseTy = elseBranch.typeOf(tenv, inferencer);

        // Both branches must have the same type (unify them).
        inferencer.unify(thenTy, elseTy,
                buildErrorMessage("if branches must have the same type."));

        // Return the (possibly substituted) type of the true branch.
        return inferencer.getSubstitutions().apply(thenTy);
    }

    /**
     * Display a AST inferencertree with the indentation specified.
     *
     * @param indentAmt the amout of indentation to perform.
     */
    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("If(", indentAmt);
        cond.displaySubtree(indentAmt + 2);
        thenBranch.displaySubtree(indentAmt + 2);
        elseBranch.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}
