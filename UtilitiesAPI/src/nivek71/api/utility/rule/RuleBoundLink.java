package nivek71.api.utility.rule;

import nivek71.api.utility.Helpers;
import nivek71.api.utility.containers.ImmutablePair;
import nivek71.api.utility.rule.rules.Rule;

import java.util.HashMap;
import java.util.Map;

public interface RuleBoundLink extends RuleBound {
    RuleBound getParentBound();

    Map<Rule, Boolean> getAllBoundsHere();

    default Boolean getRuleBoundsHere(Rule rule) {
        return getAllBoundsHere().get(rule);
    }

    @Override
    default void setRuleState(Rule rule, Boolean state) {
        if (state == null)
            getAllBoundsHere().remove(rule);
        else getAllBoundsHere().put(rule, state);
    }

    @Override
    default Boolean getRuleBounds(Rule rule) {
        return Helpers.notNullOrCompute(getRuleBoundsHere(rule), () -> getParentBound().getRuleBounds(rule));
    }

    default <T extends Rule> ImmutablePair<T, Boolean> getRuleStateHere(T rule) {
        return RuleBound.getRuleState(getAllBoundsHere(), rule);
    }

    @Override
    default <T extends Rule> ImmutablePair<T, Boolean> getRuleState(T rule) {
        ImmutablePair<T, Boolean> pair = getRuleStateHere(rule);
        return pair.getFirst() == null ? getParentBound().getRuleState(rule) : pair;
    }

    @Override
    default Map<Rule, Boolean> getAllRuleBounds() {
        // put parent bounds first, so this link can overwrite
        Map<Rule, Boolean> map = new HashMap<>(getParentBound().getAllRuleBounds());
        map.putAll(getAllBoundsHere());
        return map;
    }
}
