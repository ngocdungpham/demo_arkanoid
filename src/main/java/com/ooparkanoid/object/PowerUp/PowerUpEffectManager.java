package com.ooparkanoid.object.PowerUp;

import java.util.*;

public class PowerUpEffectManager {
    private final Map<String, ActiveEffect> activeEffects = new HashMap<>();
    private final GameContext context;

    public PowerUpEffectManager(GameContext context) {
        this.context = context;
    }

    public void activateEffect(PowerUpEffect effect, double duration) {
        String type = effect.getEffectType();

        ActiveEffect existing = activeEffects.get(type);
        if (existing != null) {
            // Chỉ reset timer, KHÔNG apply lại effect
            existing.resetTimer(duration);
            System.out.println("🔄 " + type + " refreshed! Duration reset to " + duration + "s");
        } else {
            // Apply effect mới
            effect.apply(context);
            activeEffects.put(type, new ActiveEffect(effect, duration));
            System.out.println("✨ " + type + " activated! Duration: " + duration + "s");
        }
    }

    public void update(double deltaTime) {
        Iterator<Map.Entry<String, ActiveEffect>> iterator = activeEffects.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, ActiveEffect> entry = iterator.next();
            ActiveEffect activeEffect = entry.getValue();

            activeEffect.update(deltaTime);

            if (activeEffect.isExpired()) {
                activeEffect.getEffect().remove(context);
                System.out.println("⏱️ " + entry.getKey() + " expired!");
                iterator.remove();
            }
        }
    }

    public double getRemainingTime(String effectType) {
        ActiveEffect effect = activeEffects.get(effectType);
        return effect != null ? effect.getRemainingTime() : 0;
    }

    public void clearAll() {
        for (Map.Entry<String, ActiveEffect> entry : activeEffects.entrySet()) {
            entry.getValue().getEffect().remove(context);
        }
        activeEffects.clear();
        System.out.println("🧹 All effects cleared");
    }

    public boolean isEffectActive(String effectType) {
        return activeEffects.containsKey(effectType);
    }

    private static class ActiveEffect {
        private final PowerUpEffect effect;
        private double remainingTime;

        public ActiveEffect(PowerUpEffect effect, double duration) {
            this.effect = effect;
            this.remainingTime = duration;
        }

        public void update(double deltaTime) {
            remainingTime -= deltaTime;
        }

        public void resetTimer(double duration) {
            this.remainingTime = duration;
        }

        public boolean isExpired() {
            return remainingTime <= 0;
        }

        public double getRemainingTime() {
            return remainingTime;
        }

        public PowerUpEffect getEffect() {
            return effect;
        }
    }
}
