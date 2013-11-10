package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.*;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

/**
 * Created by IntelliJ IDEA.
 * User: lee
 * Date: 16/03/2012
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */
public class AssignmentExpression extends AbstractAssignmentExpression {
    private final LValue lValue;
    private Expression rValue;
    private boolean inlined;

    public AssignmentExpression(LValue lValue, Expression rValue, boolean inlined) {
        super(lValue.getInferredJavaType());
        this.lValue = lValue;
        this.rValue = rValue;
        this.inlined = inlined;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new AssignmentExpression(cloneHelper.replaceOrClone(lValue), cloneHelper.replaceOrClone(rValue), inlined);
    }

    public void setInlined(boolean inlined) {
        this.inlined = inlined;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        lValue.collectTypeUsages(collector);
        collector.collectFrom(rValue);
    }

    @Override
    public Dumper dump(Dumper d) {
        if (inlined) d.print("(");
        d.dump(lValue).print(" = ").dump(rValue);
        if (inlined) d.print(")");
        return d;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        rValue = rValue.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        rValue = expressionRewriter.rewriteExpression(rValue, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public boolean isSelfMutatingOp1(LValue lValue, ArithOp arithOp) {
        return false;
    }

    @Override
    public ArithmeticPostMutationOperation getPostMutation() {
        throw new IllegalStateException();
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        lValueUsageCollector.collect(lValue);
        rValue.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssignmentExpression that = (AssignmentExpression) o;

        if (inlined != that.inlined) return false;
        if (lValue != null ? !lValue.equals(that.lValue) : that.lValue != null) return false;
        if (rValue != null ? !rValue.equals(that.rValue) : that.rValue != null) return false;

        return true;
    }

    @Override
    public final boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == null) return false;
        if (o == this) return true;
        if (getClass() != o.getClass()) return false;
        AssignmentExpression other = (AssignmentExpression) o;
        if (inlined != other.inlined) return false;
        if (!constraint.equivalent(lValue, other.lValue)) return false;
        if (!constraint.equivalent(rValue, other.rValue)) return false;
        return true;
    }


}
