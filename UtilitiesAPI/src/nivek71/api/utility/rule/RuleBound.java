package nivek71.api.utility.rule;

import nivek71.api.utility.containers.ImmutablePair;
import nivek71.api.utility.rule.rules.Rule;

import java.util.Collections;
import java.util.Map;

public interface RuleBound {
    RuleBound EMPTY_BOUNDS = new RuleBound() {
        @Override
        public Map<Rule, Boolean> getAllRuleBounds() {
            return Collections.emptyMap();
        }

        @Override
        public <T extends Rule> ImmutablePair<T, Boolean> getRuleState(T rule) {
            return ImmutablePair.emptyPair();
        }

        @Override
        public String toString() {
            return "RuleBound{EMPTY_BOUNDS}";
        }
    };

    Map<Rule, Boolean> getAllRuleBounds();

    /**
     * Checks the policy for the given rule.
     *
     * @param rule the rule to check
     * @return true if the rule applies; false if the rule does not apply; null if there is no policy set (in which case default should be used)
     */
    default Boolean getRuleBounds(Rule rule) {
        return getRuleState(rule).getSecond();
    }

    default <T extends Rule> ImmutablePair<T, Boolean> getRuleState(T rule) {
        return getRuleState(getAllRuleBounds(), rule);
    }

    default void setRuleState(Rule rule, Boolean state) {
        if (state == null)
            getAllRuleBounds().remove(rule);
        else getAllRuleBounds().put(rule, state);
    }

    static <T extends Rule> ImmutablePair<T, Boolean> getRuleState(Map<Rule, Boolean> map, T rule) {
        final int ruleHashCode = rule.hashCode();
        for(Map.Entry<Rule, Boolean> entry : map.entrySet()) {
            if(entry.getKey().hashCode() == ruleHashCode)
                //noinspection unchecked
                return (ImmutablePair<T, Boolean>) ImmutablePair.create(entry);
        }
        return ImmutablePair.emptyPair();
    }
}
