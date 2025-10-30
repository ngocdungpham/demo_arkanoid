package com.ooparkanoid.object.PowerUp;

import java.util.*;

public class PowerUpEffectManager {
    private final Map<String, ActiveEffect> activeEffects = new HashMap<>();
    private final GameContext context;

    public PowerUpEffectManager(GameContext context) {
        this.context = context;
    }

    public void activateEffect(PowerUpEffect effect, double duration) {
        String effectType = effect.getEffectType();
        PowerUpSprite.PowerUpCategory category = PowerUpSprite.getCategoryForEffectType(effectType);
        ActiveEffect existing = activeEffects.get(effectType);
        if (existing != null) {
            // Chỉ reset timer, KHÔNG apply lại effect
            existing.resetTimer(duration);
        } else {
            if (category != PowerUpSprite.PowerUpCategory.NONE) {
                // Xóa tất cả các hiệu ứng khác trong cùng category này
                removeAllEffectsByCategory(category, effectType);
            }
            effect.apply(context);
            activeEffects.put(effectType, new ActiveEffect(effect, category, duration));
        }
    }

    private void removeAllEffectsByCategory(PowerUpSprite.PowerUpCategory category, String newEffectType) {
        Iterator<Map.Entry<String, ActiveEffect>> iterator = activeEffects.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ActiveEffect> entry = iterator.next();
            ActiveEffect activeEffect = entry.getValue();

            if (activeEffect.getCategory() == category) {
                // Xóa hiệu ứng khỏi game
                activeEffect.getEffect().remove(context);
                iterator.remove();
            }
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
    }

    public boolean isEffectActive(String effectType) {
        return activeEffects.containsKey(effectType);
    }

    private static class ActiveEffect {
        private final PowerUpEffect effect;
        private final PowerUpSprite.PowerUpCategory category;
        private double remainingTime;

        public ActiveEffect(PowerUpEffect effect, PowerUpSprite.PowerUpCategory category, double duration) {
            this.effect = effect;
            this.category = category;
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

        public PowerUpSprite.PowerUpCategory getCategory() {
            return category;
        }
    }
}
