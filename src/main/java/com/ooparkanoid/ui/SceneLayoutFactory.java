package com.ooparkanoid.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Factory class for creating standardized UI layouts and components.
 * Provides methods for building layered scenes and reusable UI elements
 * with consistent styling and behavior across the application.
 *
 * Key Features:
 * - Layered scene creation with background, content, and overlay layers
 * - Overlay management system with registration and lifecycle control
 * - Standardized menu components (cards, headings, buttons)
 * - CSS-based styling with predefined style classes
 * - Immutable external API for thread safety
 *
 * Layered Scene Architecture:
 * - Background Layer: Static backgrounds and visual elements
 * - Content Layer: Main game content and primary UI
 * - Overlay Layer: Modal dialogs, menus, and temporary UI elements
 *
 * Usage:
 * Use createLayeredScene() to build the main scene structure,
 * then use factory methods for consistent UI components.
 * Register overlays through LayeredScene methods for dynamic content.
 *
 * @author Arkanoid Team
 * @version 2.0
 */
public final class SceneLayoutFactory {

    /** Private constructor to prevent instantiation */
    private SceneLayoutFactory() {}

    /**
     * Creates a layered scene with background, content, and overlay layers.
     * The layered structure provides separation of concerns for different UI elements.
     * Background layer contains static visual elements, content layer holds main game UI,
     * and overlay layer manages modal dialogs and temporary elements.
     *
     * @param gameContent the main game content node to place in content layer
     * @return configured LayeredScene with all layers initialized
     * @throws NullPointerException if gameContent is null
     */
    public static LayeredScene createLayeredScene(Node gameContent) {
        Objects.requireNonNull(gameContent, "gameContent");

        BackgroundLayer backgroundLayer = new BackgroundLayer();
        StackPane contentLayer = new StackPane(gameContent);
        StackPane overlayLayer = new StackPane();
        overlayLayer.setPickOnBounds(false);

        StackPane root = new StackPane(backgroundLayer, contentLayer, overlayLayer);
        root.getStyleClass().add("app");

        return new LayeredScene(root, backgroundLayer, contentLayer, overlayLayer);
    }

    /**
     * Creates a standardized menu card container with vertical layout.
     * Applies consistent styling and spacing for menu elements.
     * Cards are centered and use predefined CSS styling.
     *
     * @param children the child nodes to include in the card
     * @return configured VBox card container with menu styling
     */
    public static VBox createMenuCard(Node... children) {
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("menu-card");
        card.getChildren().addAll(children);
        return card;
    }

    /**
     * Creates a large heading label with predefined styling.
     * Uses CSS class "heading-xl" for consistent typography across the application.
     *
     * @param text the heading text to display
     * @return configured Label with heading styling
     */
    public static Label createHeading(String text) {
        Label lb = new Label(text);
        lb.getStyleClass().add("heading-xl");
        return lb;
    }

    /**
     * Creates a subtitle label with predefined styling.
     * Uses CSS class "subtitle" for secondary text elements.
     *
     * @param text the subtitle text to display
     * @return configured Label with subtitle styling
     */
    public static Label createSubtitle(String text) {
        Label lb = new Label(text);
        lb.getStyleClass().add("subtitle");
        return lb;
    }

    /**
     * Creates a pill-shaped button with predefined styling.
     * Uses CSS class "btn" for consistent button appearance across the application.
     *
     * @param text the button text to display
     * @return configured Button with pill styling
     */
    public static Button createPillButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("btn");
        return b;
    }

    /**
     * Represents a layered scene structure with background, content, and overlay layers.
     * Provides methods for managing overlays and accessing individual layers.
     * Maintains a registry of named overlays for dynamic content management.
     *
     * Layer Management:
     * - Background Layer: Static visual elements (accessed via backgroundLayer())
     * - Content Layer: Main application content (accessed via contentLayer())
     * - Overlay Layer: Modal dialogs and temporary UI (accessed via overlayLayer())
     *
     * Overlay Registry:
     * Overlays can be registered with string keys for easy management.
     * Only one overlay per key is maintained - registering a new overlay
     * with an existing key replaces the previous one.
     *
     * Thread Safety: Not thread-safe. Should be accessed from JavaFX Application Thread.
     */
    public static final class LayeredScene {
        /** Root container holding all layers */
        private final StackPane root;

        /** Background layer for static visual elements */
        private final BackgroundLayer backgroundLayer;

        /** Content layer for main application UI */
        private final StackPane contentLayer;

        /** Overlay layer for modal dialogs and temporary elements */
        private final StackPane overlayLayer;

        /** Registry mapping overlay names to their nodes */
        private final Map<String, Node> overlayRegistry = new LinkedHashMap<>();

        /**
         * Constructs a LayeredScene with the specified layers.
         * Private constructor - use SceneLayoutFactory.createLayeredScene() instead.
         *
         * @param root the root container
         * @param backgroundLayer the background layer
         * @param contentLayer the content layer
         * @param overlayLayer the overlay layer
         */
        private LayeredScene(StackPane root,
                             BackgroundLayer backgroundLayer,
                             StackPane contentLayer,
                             StackPane overlayLayer) {
            this.root = root;
            this.backgroundLayer = backgroundLayer;
            this.contentLayer = contentLayer;
            this.overlayLayer = overlayLayer;
        }

        /**
         * Gets the root container of this layered scene.
         *
         * @return the root StackPane containing all layers
         */
        public StackPane root() {
            return root;
        }

        /**
         * Gets the background layer for static visual elements.
         *
         * @return the BackgroundLayer instance
         */
        public BackgroundLayer backgroundLayer() {
            return backgroundLayer;
        }

        /**
         * Gets the content layer for main application UI.
         *
         * @return the content StackPane
         */
        public StackPane contentLayer() {
            return contentLayer;
        }

        /**
         * Gets the overlay layer for modal dialogs and temporary elements.
         *
         * @return the overlay StackPane
         */
        public StackPane overlayLayer() {
            return overlayLayer;
        }

        /**
         * Registers an overlay with the specified key.
         * If an overlay with the same key already exists, it is replaced.
         * The overlay is automatically attached to the overlay layer.
         *
         * @param key the unique identifier for this overlay
         * @param overlay the Node to register as an overlay
         * @throws NullPointerException if key or overlay is null
         */
        public void registerOverlay(String key, Node overlay) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(overlay, "overlay");

            Node existing = overlayRegistry.put(key, overlay);
            if (existing != null) {
                detach(existing);
            }
            attach(overlay);
        }

        /**
         * Unregisters an overlay by its key.
         * Removes the overlay from the registry and detaches it from the scene.
         * Does nothing if no overlay is registered with the specified key.
         *
         * @param key the key of the overlay to unregister
         */
        public void unregisterOverlay(String key) {
            Node node = overlayRegistry.remove(key);
            if (node != null) {
                detach(node);
            }
        }

        /**
         * Gets a registered overlay by its key.
         *
         * @param key the key of the overlay to retrieve
         * @return the overlay Node, or null if not found
         */
        public Node getOverlay(String key) {
            return overlayRegistry.get(key);
        }

        /**
         * Gets an immutable view of all registered overlays.
         * Changes to the returned map will not affect the registry.
         *
         * @return unmodifiable Map of overlay keys to their nodes
         */
        public Map<String, Node> getRegisteredOverlays() {
            return Collections.unmodifiableMap(overlayRegistry);
        }

        /**
         * Clears all registered overlays.
         * Detaches all overlays from the scene and clears the registry.
         */
        public void clearOverlays() {
            overlayRegistry.values().forEach(this::detach);
            overlayRegistry.clear();
        }

        /**
         * Attaches an overlay to the overlay layer.
         * Ensures the overlay is not attached to any other parent first.
         *
         * @param overlay the Node to attach
         */
        private void attach(Node overlay) {
            detachFromParent(overlay);
            overlayLayer.getChildren().add(overlay);
        }

        /**
         * Detaches an overlay from the overlay layer.
         *
         * @param overlay the Node to detach
         */
        private void detach(Node overlay) {
            overlayLayer.getChildren().remove(overlay);
        }

        /**
         * Detaches a node from its current parent container.
         * Safely removes the node from any existing parent before reattachment.
         *
         * @param node the Node to detach from its parent
         */
        private static void detachFromParent(Node node) {
            Pane parent = (Pane) node.getParent();
            if (parent != null) {
                parent.getChildren().remove(node);
            }
        }
    }
}
