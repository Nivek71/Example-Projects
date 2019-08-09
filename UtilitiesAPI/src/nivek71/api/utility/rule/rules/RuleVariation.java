package nivek71.api.utility.rule.rules;

public class RuleVariation extends Rule {
    public RuleVariation(Rule... impliedBy) {
        super(impliedBy);
    }

    // any rules with variations should be the same, provided they are of the same class
    // looking up a rule of one type should return any rule of that type (even if the state is varied).
    @Override
    public int hashCode() {
        return getClass().hashCode() + 1;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass().equals(o.getClass());
    }
}
