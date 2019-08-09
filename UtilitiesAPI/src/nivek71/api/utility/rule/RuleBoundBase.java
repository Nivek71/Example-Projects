package nivek71.api.utility.rule;

import nivek71.api.utility.rule.rules.Rule;

import java.util.HashMap;
import java.util.Map;

public class RuleBoundBase implements RuleBound {
    private final Map<Rule, Boolean> bounds = new HashMap<>();

    @Override
    public Map<Rule, Boolean> getAllRuleBounds() {
        return bounds;
    }
}
