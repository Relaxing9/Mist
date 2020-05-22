/**
 * Copyright © 2020 Property of Illuzionz Studios, LLC
 * All rights reserved. No part of this publication may be reproduced, distributed, or
 * transmitted in any form or by any means, including photocopying, recording, or other
 * electronic or mechanical methods, without the prior written permission of the publisher,
 * except in the case of brief quotations embodied in critical reviews and certain other
 * noncommercial uses permitted by copyright law. Any licensing of this software overrides
 * this statement.
 */
package com.illuzionzstudios.mist.config;

import com.illuzionzstudios.mist.config.format.Comment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A section of memory that relates to a configuration
 * See {@link MemoryConfiguration}
 */
public class ConfigSection extends MemoryConfiguration {

    //  -------------------------------------------------------------------------
    //  Final variables
    //  -------------------------------------------------------------------------

    /**
     * This is the full node path to this section.
     * For instance if this section is for a player, the
     * full path may be "data.player.<player_name>"
     */
    @Getter
    private final String fullPath;

    /**
     * This is the relevant node for the section, could
     * also be called the name. For instance take the above
     * example for {@link #fullPath}, this would be "<player_name>"
     */
    @Getter
    private final String nodeKey;

    /**
     * This is the main {@link ConfigSection} for the file
     * All sections come under it
     */
    @Getter
    private final ConfigSection root;

    /**
     * This is the {@link ConfigSection} that this section
     * is under
     */
    @Getter
    private final ConfigSection parent;

    /**
     * These are the current {@link Comment} loaded for this section
     * Each comment is mapped to the absolute path to the value
     */
    @Getter
    private final HashMap<String, Comment> configComments;

    /**
     * These are the default {@link Comment} for each value. These are
     * loaded when default values are set. Again each comment
     * is mapped to the absolute path to the value
     */
    @Getter
    private final HashMap<String, Comment> defaultComments;

    /**
     * These are the loaded key value pairs for this {@link ConfigSection}
     */
    @Getter
    private final Map<String, Object> values;

    /**
     * These are the default key value pairs for this {@link ConfigSection}
     * They're what are automatically loaded if no values are found
     */
    @Getter
    private final Map<String, Object> defaults;

    /**
     * Flag if this section is a default {@link ConfigSection}
     * Meaning it gets loaded into the file if not found
     */
    @Getter
    private final boolean isDefault;

    /**
     * This object here is invoked on by the {@code synchronized} tag
     * This is to lock performing operations on our section across threads.
     * This is because concurrently editing the config may lead to weird things
     * being saved or our data not being saved properly.
     *
     * Each {@link ConfigSection} contains their own lock because we only
     * need one lock per instance, as we can edit different instances at
     * the different times. Although, we generally invoke on the {@link #root}
     * {@link ConfigSection} as we want to lock each actual {@link YamlConfig} instance
     *
     * Methods that invoke this (we limit) are:
     * {@link #createNodePath(String, boolean)}
     */
    private final Object lock = new Object();

    //  -------------------------------------------------------------------------
    //  Values we may want to change
    //  -------------------------------------------------------------------------

    /**
     * The amount of SPACE chars to use as indentation.
     * This means space of each key from the parent section
     */
    @Getter
    @Setter
    protected int indentation = 2;

    /**
     * This is the character to separate the paths with.
     * For instance if set to '.', paths will be "foo.bar".
     * And again '#', "foo#bar"
     *
     * IMPORTANT: Must not be set when the config is currently
     * loaded or when adding {@link ConfigSection}. This is because
     * paths may then different with separators and produce a whole
     * bunch of errors and be a pain to debug
     */
    @Getter
    @Setter
    protected char pathSeparator = '.';

    /**
     * Flag is set to true if changes were made the the section at all
     * Useful for detecting to save
     */
    @Getter
    private boolean changed = false;

    /**
     * Init blank config section
     */
    public ConfigSection() {
        this.root = this;
        this.parent = this;

        this.isDefault = false;
        this.nodeKey = "";
        this.fullPath = "";

        this.configComments = new HashMap<>();
        this.defaultComments = new HashMap<>();
        this.values = new LinkedHashMap<>();
        this.defaults = new LinkedHashMap<>();
    }

    /**
     * Setup the config section in another {@link ConfigSection}
     *
     * @param root The absolute root {@link ConfigSection}
     * @param parent The {@link ConfigSection} just above {@link this}
     * @param nodeKey See {@link #nodeKey}
     * @param isDefault See {@link #isDefault}
     */
    public ConfigSection(ConfigSection root, ConfigSection parent, String nodeKey, boolean isDefault) {
        this.root = root;
        this.parent = parent;
        this.nodeKey = nodeKey;
        this.fullPath = nodeKey != null ? parent.fullPath + nodeKey + root.pathSeparator : parent.fullPath;
        this.isDefault = isDefault;
        configComments = defaultComments = null;
        defaults = null;
        values = null;
    }

    /**
     * @return Sanitized {@link #fullPath}
     */
    public String getKey() {
        return !fullPath.endsWith(String.valueOf(root.pathSeparator)) ? fullPath : fullPath.substring(0, fullPath.length() - 1);
    }

    /**
     * This is used to create the {@link ConfigSection} for a node. This is in order
     * to set values and avoid null errors.
     * <b>DON'T INVOKE ON THE {@link #lock} OBJECT</b>
     *
     * @param path Full path to the node for the value. Eg, foo.bar.node, this will create
     *             a {@link ConfigSection} for "foo" and "foo.bar"
     * @param useDefault If the value to be set at this node is a default value
     */
    protected void createNodePath(@NotNull String path, boolean useDefault) {
        // Make sure our path separator is valid
        if (path.indexOf(root.pathSeparator) != -1) {
            // If any nodes leading to this full path don't exist, create them
            String[] pathParts = path.split(Pattern.quote(String.valueOf(root.pathSeparator)));
            StringBuilder nodePath = new StringBuilder(fullPath);

            // If creating default path, write the nodes to defaults
            Map<String, Object> writeTo = useDefault ? root.defaults : root.values;
            Objects.requireNonNull(writeTo, "Can't write to invalid value map");

            // Last node that was set
            ConfigSection travelNode = this;
            synchronized (root.lock) {
                // For each node to full path
                for (int i = 0; i < pathParts.length - 1; ++i) {
                    // Create the current node
                    final String node = (i != 0 ? nodePath.append(root.pathSeparator) : nodePath).append(pathParts[i]).toString();

                    // If not set as a config section, set it
                    if (!(writeTo.get(node) instanceof ConfigSection)) {
                        writeTo.put(node, travelNode = new ConfigSection(root, travelNode, pathParts[i], useDefault));
                    } else {
                        // Else just set our current node the mapped node
                        travelNode = (ConfigSection) writeTo.get(node);
                    }
                }
            }
        }
    }

    /**
     * This will create a {@link ConfigSection} that acts as a default that must
     * appear in the section. Also optional comments for this section
     *
     * @param path The relevant path from this {@link ConfigSection} to the new {@link ConfigSection}
     *             to create
     * @param comment Varargs of comments to explain this section
     * @return The created {@link ConfigSection} for this path
     */
    @NotNull
    public ConfigSection createDefaultSection(@NotNull String path, String... comment) {
        createNodePath(path, true);

        // Create the section
        ConfigSection section = new ConfigSection(root, this, path, true);

        // Assure not null
        Objects.requireNonNull(root.defaults, "Root config has invalid default values map");
        Objects.requireNonNull(root.defaultComments, "Root config has invalid default comments map");

        // Insert into root maps
        synchronized (root.lock) {
            root.defaults.put(fullPath + path, section);
            root.defaultComments.put(fullPath + path, new Comment(comment));
        }
        return section;
    }

    /**
     * See {@link #createDefaultSection(String, String...)}
     *
     * @param style The custom {@link com.illuzionzstudios.mist.config.format.Comment.CommentStyle} for the comments
     */
    @NotNull
    public ConfigSection createDefaultSection(@NotNull String path, Comment.CommentStyle style, String... comment) {
        createNodePath(path, true);

        // Create the section
        ConfigSection section = new ConfigSection(root, this, path, true);

        // Assure not null
        Objects.requireNonNull(root.defaults, "Root config has invalid default values map");
        Objects.requireNonNull(root.defaultComments, "Root config has invalid default comments map");

        // Insert into root maps
        synchronized (root.lock) {
            root.defaults.put(fullPath + path, section);
            root.defaultComments.put(fullPath + path, new Comment(style, comment));
        }
        return section;
    }

}
