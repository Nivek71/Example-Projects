package nivek71.api.utility.rule.rules;

import nivek71.api.utility.Helpers;
import nivek71.api.utility.functions.ConsumerEx;
import nivek71.api.utility.functions.RunnableEx;
import nivek71.api.utility.rule.RuleBound;
import org.apache.commons.lang.Validate;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Rule {
    private static final Map<Class<?>, Function<Object, RuleBound>> typeBindPolicies = new HashMap<>();
    private static final Map<Object, RuleBound> boundObjects = new HashMap<>();
    private final Rule[] impliedBy;
    private boolean setByDefault = false;

    public Rule(Rule... impliedBy) {
        this.impliedBy = impliedBy;
    }

    public boolean isSetByDefault() {
        return setByDefault;
    }

    public void setByDefault(boolean setByDefault) {
        this.setByDefault = setByDefault;
    }

    public Boolean getRuleStatus(RuleBound bound) {
        Boolean value = bound.getRuleBounds(this);
        // if this value is explicitly set, return that explicit value
        if (value != null)
            return value;
        for (Rule implied : impliedBy) {
            value = implied.getRuleStatus(bound);
            // if an implied rule is enabled, this rule is enabled
            if (value != null && value)
                return true;
        }
        // this rule was not set, and any rules which may imply this rule did not imply this rule; no preference
        return null;
    }

    public boolean getRuleState(RuleBound bound) {
        Boolean status = getRuleStatus(bound);
        return status == null ? isSetByDefault() : status;
    }

    public void enforceRule(RuleBound bound, boolean invert, RunnableEx enforcer) {
        if (getRuleState(bound) != invert) {
            enforcer.run();
        }
    }

    public <T extends Event> Rule addRuleEnforcer(Plugin plugin, Class<T> eventCl, boolean invert, Function<T, Object> relevantObject, ConsumerEx<T> handler) {
        // register event handler
        Helpers.registerDynamicEvent(plugin, eventCl, event -> {
            // superclass event handlers are called when a subclass is passed through;
            // only handle when this event object is not a subclass of T
            // todo - what if there is no subclass handler? this is not a perfect implementation; if a subclass event
            // todo - gets added in a later API version, the rule enforcer will not pick up on it, unless a new rule
            // todo - is defined for the subclass event. This should probably be fixed, though it is a low priority issue
            if (eventCl != event.getClass()) {
                return;
            }

            RuleBound bound = getObjectBounds(relevantObject.apply(event));
            enforceRule(bound, invert, () -> handler.acceptEx(event));
        }, EventPriority.LOW, false);
        return this;
    }

    public <T extends PlayerEvent> Rule addPlayerRuleEnforcer(Plugin plugin, Class<T> eventCl, boolean invert, ConsumerEx<T> handler) {
        return addRuleEnforcer(plugin, eventCl, invert, PlayerEvent::getPlayer, handler);
    }

    public <T extends EntityEvent> Rule addEntityRuleEnforcer(Plugin plugin, Class<T> eventCl, boolean invert, ConsumerEx<T> handler) {
        return addRuleEnforcer(plugin, eventCl, invert, EntityEvent::getEntity, handler);
    }

    public <T extends PlayerEvent & Cancellable> Rule addPlayerRuleEnforcer(Plugin plugin, Class<T> eventCl, boolean invert) {
        return addPlayerRuleEnforcer(plugin, eventCl, invert, event -> event.setCancelled(true));
    }

    public <T extends EntityEvent & Cancellable> Rule addEntityRuleEnforcer(Plugin plugin, Class<T> eventCl, boolean invert) {
        return addEntityRuleEnforcer(plugin, eventCl, invert, event -> event.setCancelled(true));
    }

    public static RuleBound getObjectBounds(Object object) {
        return boundObjects.getOrDefault(object, object == null ? RuleBound.EMPTY_BOUNDS : getBoundPoliciesFor(object.getClass()).apply(Helpers.helper(object)));
    }

    public static RuleBound setRuleBounds(Object object, RuleBound bound) {
        Validate.notNull(object, "Cannot set bounds on null");
        if (bound == null)
            return boundObjects.remove(object);
        return boundObjects.put(object, bound);
    }

    public static <T> Function<T, RuleBound> getBoundPoliciesFor(Class<T> cl) {
        //noinspection unchecked
        return (Function<T, RuleBound>) typeBindPolicies.getOrDefault(cl, type -> type instanceof RuleBound ? (RuleBound) type : RuleBound.EMPTY_BOUNDS);
    }

    @SuppressWarnings("unchecked")
    public static <T> Function<T, RuleBound> setBoundPoliciesFor(Class<T> cl, Function<T, RuleBound> bound) {
        Validate.notNull(cl, "Cannot set bounds on type null");
        if (bound == null)
            return (Function<T, RuleBound>) typeBindPolicies.remove(cl);
        return (Function<T, RuleBound>) typeBindPolicies.put(cl, Helpers.helper(bound));
    }
}