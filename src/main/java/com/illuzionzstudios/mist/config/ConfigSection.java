package com.illuzionzstudios.mist.config;

import com.illuzionzstudios.mist.config.format.Comment;
import com.illuzionzstudios.mist.config.format.CommentStyle;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * A section of memory that relates to a configuration. This configuration
 * is usually a YAML file which is split into different memory sections
 *
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
    protected final ConfigSection root;

    /**
     * This is the {@link ConfigSection} that this section
     * is under
     */
    @Getter
    protected final ConfigSection parent;

    /**
     * These are the current {@link Comment} loaded for this section
     * Each comment is mapped to the absolute path to the value
     */
    @Getter
    protected final HashMap<String, Comment> configComments;

    /**
     * These are the default {@link Comment} for each value. These are
     * loaded when default values are set. Again each comment
     * is mapped to the absolute path to the value
     */
    @Getter
    protected final HashMap<String, Comment> defaultComments;

    /**
     * These are the loaded key value pairs for this {@link ConfigSection}
     */
    @Getter
    protected final Map<String, Object> values;

    /**
     * These are the default key value pairs for this {@link ConfigSection}
     * They're what are automatically loaded if no values are found
     */
    protected final Map<String, Object> defaults;

    /**
     * Flag if this section is a default {@link ConfigSection}
     * Meaning it gets loaded into the file if not found
     */
    @Getter
    protected final boolean isDefault;

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
     */
    protected final Object lock = new Object();

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
    protected boolean changed = false;

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
     * @param root The absolute root {@link ConfigSection} (Main file)
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
     * This method is invoked everytime we make manual changes to
     * values in the code. This is so we can make any operations
     * or update data when we make changes.
     *
     * Can be overridden to setup our own stuff when making changes
     */
    protected void onChange() {
        // Also call change on main root section
        if (root != null && root != this) {
            root.onChange();
        }
        // Reset
        this.changed = false;
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

    //  -------------------------------------------------------------------------
    //  Section utils
    //  -------------------------------------------------------------------------

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
     * @param style The custom {@link CommentStyle} for the comments
     */
    @NotNull
    public ConfigSection createDefaultSection(@NotNull String path, CommentStyle style, String... comment) {
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

    //  -------------------------------------------------------------------------
    //  Help with manipulation of comments
    //  -------------------------------------------------------------------------

    /**
     * See {@link #setComment(String, Comment)} and construct {@link Comment} from parameters
     * 
     * @param commentStyle The styling for the comment
     * @param lines The lines to set
     */
    @NotNull
    public ConfigSection setComment(@NotNull String path, @Nullable CommentStyle commentStyle, String... lines) {
        return setComment(path, lines != null ? new Comment(commentStyle, lines) : null);
    }

    /**
     * See {@link #setComment(String, CommentStyle, String...)}
     */
    @NotNull
    public ConfigSection setComment(@NotNull String path, @Nullable CommentStyle commentStyle, @Nullable List<String> lines) {
        return setComment(path, lines != null ? new Comment(commentStyle, lines) : null);
    }

    /**
     * Set a {@link Comment} for a node of a {@link ConfigSection}
     *
     * @param path The relevant path to the node to set
     * @param comment The {@link Comment} object to set
     * @return The {@link ConfigSection} the comment was set for
     */
    @NotNull
    public ConfigSection setComment(@NotNull String path, @Nullable Comment comment) {
        // Assure not null
        Objects.requireNonNull(root.defaultComments, "Root config has invalid default comments map");
        Objects.requireNonNull(root.configComments, "Root config has invalid config comments map");

        synchronized (root.lock) {
            if (isDefault) {
                root.defaultComments.put(fullPath + path, comment);
            } else {
                root.configComments.put(fullPath + path, comment);
            }
        }
        return this;
    }

    /**
     * See {@link #setDefaultComment(String, List)}
     */
    @NotNull
    public ConfigSection setDefaultComment(@NotNull String path, String... lines) {
        return setDefaultComment(path, lines.length == 0 ? null : Arrays.asList(lines));
    }

    /**
     * See {@link #setDefaultComment(String, Comment)}
     */
    @NotNull
    public ConfigSection setDefaultComment(@NotNull String path, @Nullable List<String> lines) {
        setDefaultComment(fullPath + path, new Comment(lines));
        return this;
    }

    /**
     * See {@link #setDefaultComment(String, CommentStyle, List)}
     */
    @NotNull
    public ConfigSection setDefaultComment(@NotNull String path, CommentStyle commentStyle, String... lines) {
        return setDefaultComment(path, commentStyle, lines.length == 0 ? null : Arrays.asList(lines));
    }

    /**
     * See {@link #setDefaultComment(String, List)} but we set {@link CommentStyle}
     * for the comments
     */
    @NotNull
    public ConfigSection setDefaultComment(@NotNull String path, CommentStyle commentStyle, @Nullable List<String> lines) {
        setDefaultComment(fullPath + path, new Comment(commentStyle, lines));
        return this;
    }

    /**
     * See {@link #setComment(String, Comment)} but we are setting default values,
     * so mapped to {@link #defaultComments}
     */
    @NotNull
    public ConfigSection setDefaultComment(@NotNull String path, @Nullable Comment comment) {
        Objects.requireNonNull(root.defaultComments, "Root config has invalid default comments map");

        synchronized (root.lock) {
            root.defaultComments.put(fullPath + path, comment);
        }
        return this;
    }

    /**
     * Get the {@link Comment} instance from a relevant node path
     * May produce {@code null}
     *
     * @param path The relevant path to the value
     * @return The {@link Comment} for the {@link ConfigSection} if applicable
     */
    @Nullable
    public Comment getComment(@NotNull String path) {
        Objects.requireNonNull(root.defaultComments, "Root config has invalid default comments map");
        Objects.requireNonNull(root.configComments, "Root config has invalid config comments map");

        Comment result = root.configComments.get(fullPath + path);
        if (result == null) {
            result = root.defaultComments.get(fullPath + path);
        }
        return result;
    }

    /**
     * See {@link #getComment(String)}
     *
     * @return {@link Comment} invoked with {@link Comment#toString()}
     *          May produce {@code null}
     */
    @Nullable
    public String getCommentString(@NotNull String path) {
        Comment result = getComment(path);
        return result != null ? result.toString() : null;
    }

    //  -------------------------------------------------------------------------
    //  Methods to get sections and keys
    //  -------------------------------------------------------------------------

    /**
     * This method will create a default value for a specific node path
     *
     * @param path The relative path to add the default to
     * @param value The value to set for this node
     */
    @Override
    public void addDefault(@NotNull String path, @Nullable Object value) {
        Objects.requireNonNull(root.defaults, "Root config has invalid default values map");

        createNodePath(path, true);
        synchronized (root.lock) {
            root.defaults.put(fullPath + path, value);
        }
    }

    /**
     * @return A new {@link ConfigSection} with this as a parent as a default section
     */
    @Override
    public ConfigSection getDefaults() {
        return new ConfigSection(root, this, null, true);
    }

    /**
     * @param configuration Set the default configuration adapter
     */
    @Override
    public void setDefaults(@NotNull Configuration configuration) {
        Objects.requireNonNull(root.defaults, "Root config has invalid default values map");

        if (fullPath.isEmpty()) {
            root.defaults.clear();
        } else {
            root.defaults.keySet().stream()
                    .filter(k -> k.startsWith(fullPath))
                    .forEach(root.defaults::remove);
        }
        addDefaults(configuration);
    }

    /**
     * @return See {@link #getDefaults()}
     */
    @Override
    public ConfigSection getDefaultSection() {
        return getDefaults();
    }

    /**
     * Used to get all the node paths set for values. This is only for every path under this
     * {@link ConfigSection}. For instance if there is "foo.bar" and "bar.foo", will return foo and bar.
     * If the deep option is set, will return foo foo.bar bar bar.foo
     *
     * @param deep If to recursive search for nodes otherwise returns full paths
     * @return A set of path nodes as a {@link String}
     */
    @NotNull
    @Override
    public Set<String> getKeys(boolean deep) {
        Objects.requireNonNull(root.defaults, "Root config has invalid default values map");
        Objects.requireNonNull(root.values, "Root config has invalid values map");

        // Set of keys
        LinkedHashSet<String> result = new LinkedHashSet<>();
        int pathIndex = fullPath.lastIndexOf(root.pathSeparator);

        if (deep) {
            result.addAll(root.defaults.keySet().stream()
                    .filter(k -> k.startsWith(fullPath))
                    .map(k -> !k.endsWith(String.valueOf(root.pathSeparator)) ? k.substring(pathIndex + 1) : k.substring(pathIndex + 1, k.length() - 1))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
            result.addAll(root.values.keySet().stream()
                    .filter(k -> k.startsWith(fullPath))
                    .map(k -> !k.endsWith(String.valueOf(root.pathSeparator)) ? k.substring(pathIndex + 1) : k.substring(pathIndex + 1, k.length() - 1))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        } else {
            result.addAll(root.defaults.keySet().stream()
                    .filter(k -> k.startsWith(fullPath) && k.lastIndexOf(root.pathSeparator) == pathIndex)
                    .map(k -> !k.endsWith(String.valueOf(root.pathSeparator)) ? k.substring(pathIndex + 1) : k.substring(pathIndex + 1, k.length() - 1))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
            result.addAll(root.values.keySet().stream()
                    .filter(k -> k.startsWith(fullPath) && k.lastIndexOf(root.pathSeparator) == pathIndex)
                    .map(k -> !k.endsWith(String.valueOf(root.pathSeparator)) ? k.substring(pathIndex + 1) : k.substring(pathIndex + 1, k.length() - 1))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        return result;
    }

    /**
     * See {@link #getKeys(boolean)}
     *
     * Will do the same but instead map the nodes to the value found at that path
     *
     * @return A map of nodes to their values
     */
    @NotNull
    @Override
    public Map<String, Object> getValues(boolean deep) {
        Objects.requireNonNull(root.defaults, "Root config has invalid default values map");
        Objects.requireNonNull(root.values, "Root config has invalid values map");

        Function<Integer, Collector<Map.Entry<String, Object>, ?, LinkedHashMap<String, Object>>> collectorFunction = pathIndex1 -> Collectors.toMap(
                e -> !e.getKey().endsWith(String.valueOf(root.pathSeparator)) ? e.getKey().substring(pathIndex1 + 1) : e.getKey().substring(pathIndex1 + 1, e.getKey().length() - 1),
                Map.Entry::getValue,
                (v1, v2) -> {
                    throw new IllegalStateException();
                }, // never going to be merging keys
                LinkedHashMap::new);

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        int pathIndex = fullPath.lastIndexOf(root.pathSeparator);
        if (deep) {
            result.putAll(root.defaults.entrySet().stream()
                    .filter(k -> k.getKey().startsWith(fullPath))
                    .collect(collectorFunction.apply(pathIndex)));
            result.putAll(root.values.entrySet().stream()
                    .filter(k -> k.getKey().startsWith(fullPath))
                    .collect(collectorFunction.apply(pathIndex)));
        } else {
            result.putAll(root.defaults.entrySet().stream()
                    .filter(k -> k.getKey().startsWith(fullPath) && k.getKey().lastIndexOf(root.pathSeparator) == pathIndex)
                    .collect(collectorFunction.apply(pathIndex)));
            result.putAll(root.values.entrySet().stream()
                    .filter(k -> k.getKey().startsWith(fullPath) && k.getKey().lastIndexOf(root.pathSeparator) == pathIndex)
                    .collect(collectorFunction.apply(pathIndex)));
        }
        return result;
    }

    /**
     * See {@link #getKeys(boolean)}
     *
     * This will perform a shallow search for all keys and
     * add all found {@link ConfigSection} for that node path
     *
     * @param path The relative path to find
     * @return A list of found {@link ConfigSection}
     */
    @NotNull
    public List<ConfigSection> getSections(String path) {
        ConfigSection rootSection = getConfigurationSection(path);

        if (rootSection == null) {
            return Collections.EMPTY_LIST;
        }

        ArrayList<ConfigSection> result = new ArrayList<>();
        rootSection.getKeys(false).stream()
                .map(rootSection::get)
                .filter(object -> object instanceof ConfigSection)
                .forEachOrdered(object -> result.add((ConfigSection) object));
        return result;
    }

    /**
     * Check if a value is set at a node path, ie, path is in our value map
     *
     * @param path The path to check
     * @return Whether their is a value set there
     */
    @Override
    public boolean contains(@NotNull String path) {
        Objects.requireNonNull(root.defaults, "Root config has invalid default values map");
        Objects.requireNonNull(root.values, "Root config has invalid values map");

        return root.defaults.containsKey(fullPath + path) || root.values.containsKey(fullPath + path);
    }

    /**
     * See {@link #contains(String)}
     *
     * @param ignoreDefault If to not check the defaults as well
     */
    @Override
    public boolean contains(@NotNull String path, boolean ignoreDefault) {
        Objects.requireNonNull(root.defaults, "Root config has invalid default values map");
        Objects.requireNonNull(root.values, "Root config has invalid values map");

        return (!ignoreDefault && root.defaults.containsKey(fullPath + path)) || root.values.containsKey(fullPath + path);
    }

    /**
     * See {@link #contains(String)} except checks if value set is not null, ie, an actual value set
     */
    @Override
    public boolean isSet(@NotNull String path) {
        Objects.requireNonNull(root.defaults, "Root config has invalid default values map");
        Objects.requireNonNull(root.values, "Root config has invalid values map");

        return root.defaults.get(fullPath + path) != null || root.values.get(fullPath + path) != null;
    }

    /**
     * @return Sanitized {@link #fullPath} without {@link #pathSeparator}
     */
    @NotNull
    @Override
    public String getCurrentPath() {
        return fullPath.isEmpty() ? "" : fullPath.substring(0, fullPath.length() - 1);
    }

    /**
     * @return See {@link #nodeKey}
     */
    @NotNull
    @Override
    public String getName() {
        return getNodeKey();
    }

    //  -------------------------------------------------------------------------
    //  Getting and setting values in the config
    //  -------------------------------------------------------------------------

    /**
     * Simplest way to get a value from the {@link ConfigSection}
     * If value was found at path, simply returns as {@link Object},
     * this means we need to do our own casting
     *
     * @param path The path to search for
     * @return Found object, could be {@code null}
     */
    @Nullable
    @Override
    public Object get(@NotNull String path) {
        Objects.requireNonNull(root.defaults, "Root config has invalid default values map");
        Objects.requireNonNull(root.values, "Root config has invalid values map");

        Object result = root.values.get(fullPath + path);
        if (result == null) {
            result = root.defaults.get(fullPath + path);
        }
        return result;
    }

    /**
     * See {@link #get(String)}
     *
     * Able to provide a default value that get returns should nothing be
     * found at the path. Also doesn't search in defaults if not found, instead
     * returns our def.
     *
     * @param path The path to search for
     * @param def Default object to return should one not be found
     * @return Found object or default
     */
    @Nullable
    @Override
    public Object get(@NotNull String path, @Nullable Object def) {
        Objects.requireNonNull(root.values, "Root config has invalid values map");

        Object result = root.values.get(fullPath + path);
        return result != null ? result : def;
    }

    /**
     * See {@link #get(String)}
     *
     * In this instance we provide a type and ensure our found object
     * get's returned as this type. Can still produce {@code null}
     *
     * If object isn't instance of T, simply return null
     *
     * @param path The path to search for
     * @param type The class of T for casting
     * @param <T> The type that the found object must be
     * @return Found value as T
     */
    @Nullable
    public <T> T getT(@NotNull String path, final Class<T> type) {
        return getT(path, type, null);
    }

    /**
     * See {@link #getT(String, Class)}
     *
     * Except we are able to return a default instance of T should the value
     * not be found or not be able to cast to T
     */
    @Nullable
    public <T> T getT(@NotNull String path, final Class<T> type, T def) {
        Object raw = get(path);
        return type.isInstance(raw) ? type.cast(raw) : def;
    }

    /**
     * A simple way to set a value in the config. It will set the value
     * at a given path. It then checks for nodes without a value to free up
     * memory and make sure if we set anything null, it's removed
     *
     * @param path Path to set value at
     * @param value Object to place as a value. Setting to {@code null} removes value from memory
     */
    @Override
    public void set(@NotNull String path, @Nullable Object value) {
        Objects.requireNonNull(root.values, "Root config has invalid values map");

        if (isDefault) {
            // If it's a default section, set a default value
            addDefault(path, value);
        } else {
            createNodePath(path, false);

            // Attempt to set current value
            Object last;
            synchronized (root.lock) {
                if (value != null) {
                    root.changed |= (last = root.values.put(fullPath + path, value)) != value;
                } else {
                    root.changed |= (last = root.values.remove(fullPath + path)) != null;
                }
            }

            if (last != value && last instanceof ConfigSection) {
                // Then try remove nodes that don't have a value set in the config anymore
                // This is in case we set this value to null
                final String trim = fullPath + path + root.pathSeparator;
                synchronized (root.lock) {
                    root.values.keySet().stream().filter(k -> k.startsWith(trim)).collect(Collectors.toSet())
                            .forEach(root.values::remove);
                }
            }
            onChange();
        }
    }

    /**
     * See {@link #set(String, Object)} and {@link #setComment(String, CommentStyle, String...)}
     */
    @NotNull
    public ConfigSection set(@NotNull String path, @Nullable Object value, String... comment) {
        set(path, value);
        return setComment(path, null, comment);
    }

    /**
     * See {@link #set(String, Object, String...)}
     */
    @NotNull
    public ConfigSection set(@NotNull String path, @Nullable Object value, List<String> comment) {
        set(path, value);
        return setComment(path, null, comment);
    }

    /**
     * See {@link #set(String, Object, List)} but set comment styling
     */
    @NotNull
    public ConfigSection set(@NotNull String path, @Nullable Object value, @Nullable CommentStyle commentStyle, String... comment) {
        set(path, value);
        return setComment(path, commentStyle, comment);
    }

    /**
     * See {@link #set(String, Object, CommentStyle, String...)}
     */
    @NotNull
    public ConfigSection set(@NotNull String path, @Nullable Object value, @Nullable CommentStyle commentStyle, List<String> comment) {
        set(path, value);
        return setComment(path, commentStyle, comment);
    }

    /**
     * See {@link #addDefault(String, Object)}
     */
    @NotNull
    public ConfigSection setDefault(@NotNull String path, @Nullable Object value) {
        addDefault(path, value);
        return this;
    }

    /**
     * See {@link #setDefault(String, Object)} and {@link #setDefaultComment(String, String...)}
     */
    @NotNull
    public ConfigSection setDefault(@NotNull String path, @Nullable Object value, String... comment) {
        addDefault(path, value);
        return setDefaultComment(path, comment);
    }

    /**
     * See {@link #setDefault(String, Object, String...)}
     */
    @NotNull
    public ConfigSection setDefault(@NotNull String path, @Nullable Object value, List<String> comment) {
        addDefault(path, value);
        return setDefaultComment(path, comment);
    }

    /**
     * See {@link #set(String, Object, CommentStyle, String...)} but default
     */
    @NotNull
    public ConfigSection setDefault(@NotNull String path, @Nullable Object value, CommentStyle commentStyle, String... comment) {
        addDefault(path, value);
        return setDefaultComment(path, commentStyle, comment);
    }

    /**
     * See {@link #setDefault(String, Object, CommentStyle, String...)}
     */
    @NotNull
    public ConfigSection setDefault(@NotNull String path, @Nullable Object value, CommentStyle commentStyle, List<String> comment) {
        addDefault(path, value);
        return setDefaultComment(path, commentStyle, comment);
    }

    /**
     * See {@link #set(String, Object)}
     *
     * This does the same thing except we are setting a new {@link ConfigSection}
     * This may be if we want to construct things under it and bulk update
     *
     * @param path The path to set for
     * @return The set {@link ConfigSection}
     */
    @NotNull
    @Override
    public ConfigSection createSection(@NotNull String path) {
        Objects.requireNonNull(root.values, "Root config has invalid values map");

        createNodePath(path, false);
        ConfigSection section = new ConfigSection(root, this, path, false);

        synchronized (root.lock) {
            root.values.put(fullPath + path, section);
        }

        // Added section so changed
        root.changed = true;
        onChange();
        return section;
    }


    @NotNull
    public ConfigSection createSection(@NotNull String path, String... comment) {
        return createSection(path, null, comment.length == 0 ? null : Arrays.asList(comment));
    }

    @NotNull
    public ConfigSection createSection(@NotNull String path, @Nullable List<String> comment) {
        return createSection(path, null, comment);
    }

    @NotNull
    public ConfigSection createSection(@NotNull String path, @Nullable CommentStyle commentStyle, String... comment) {
        return createSection(path, commentStyle, comment.length == 0 ? null : Arrays.asList(comment));
    }

    /**
     * See {@link #createSection(String)} and {@link #set(String, Object, CommentStyle, String...)}
     *
     * Except we are doing this on a new {@link ConfigSection}
     */
    @NotNull
    public ConfigSection createSection(@NotNull String path, @Nullable CommentStyle commentStyle, @Nullable List<String> comment) {
        Objects.requireNonNull(root.values, "Root config has invalid values map");

        createNodePath(path, false);
        ConfigSection section = new ConfigSection(root, this, path, false);

        synchronized (root.lock) {
            root.values.put(fullPath + path, section);
        }

        setComment(path, commentStyle, comment);
        root.changed = true;
        onChange();
        return section;
    }

    /**
     * See {@link #createSection(String)}
     *
     * Except we are able to map node value pairs to this section already
     */
    @NotNull
    @Override
    public ConfigSection createSection(@NotNull String path, Map<?, ?> map) {
        Objects.requireNonNull(root.values, "Root config has invalid values map");

        createNodePath(path, false);
        ConfigSection section = new ConfigSection(root, this, path, false);

        synchronized (root.lock) {
            root.values.put(fullPath + path, section);
        }

        // Map into section
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                section.createSection(entry.getKey().toString(), (Map<?, ?>) entry.getValue());
                continue;
            }
            section.set(entry.getKey().toString(), entry.getValue());
        }

        root.changed = true;
        onChange();
        return section;
    }

    //  -------------------------------------------------------------------------
    //  Type safe getters
    //  -------------------------------------------------------------------------

    @Nullable
    @Override
    public String getString(@NotNull String path) {
        return getT(path, String.class, "");
    }

    @Nullable
    @Override
    public String getString(@NotNull String path, @Nullable String def) {
        return getT(path, String.class, def);
    }

    public char getChar(@NotNull String path) {
        return getT(path, Character.class, '\0');
    }

    public char getChar(@NotNull String path, char def) {
        return getT(path, Character.class, def);
    }

    @Override
    public int getInt(@NotNull String path) {
        Object result = get(path);
        return result instanceof Number ? ((Number) result).intValue() : 0;
    }

    @Override
    public int getInt(@NotNull String path, int def) {
        Object result = get(path);
        return result instanceof Number ? ((Number) result).intValue() : def;
    }

    @Override
    public boolean getBoolean(@NotNull String path) {
        return getT(path, Boolean.class, false);
    }

    @Override
    public boolean getBoolean(@NotNull String path, boolean def) {
        return getT(path, Boolean.class, def);
    }

    @Override
    public double getDouble(@NotNull String path) {
        Object result = get(path);
        return result instanceof Number ? ((Number) result).doubleValue() : 0;
    }

    @Override
    public double getDouble(@NotNull String path, double def) {
        Object result = get(path);
        return result instanceof Number ? ((Number) result).doubleValue() : def;
    }

    @Override
    public long getLong(@NotNull String path) {
        Object result = get(path);
        return result instanceof Number ? ((Number) result).longValue() : 0;
    }

    @Override
    public long getLong(@NotNull String path, long def) {
        Object result = get(path);
        return result instanceof Number ? ((Number) result).longValue() : def;
    }

    @Nullable
    @Override
    public List<?> getList(@NotNull String path) {
        return getT(path, List.class);
    }

    @Nullable
    @Override
    public List<?> getList(@NotNull String path, @Nullable List<?> def) {
        return getT(path, List.class, def);
    }

    @Override
    public ConfigSection getConfigurationSection(@NotNull String path) {
        return getT(path, ConfigSection.class);
    }

    /**
     * See {@link #getConfigurationSection(String)}
     *
     * Except it will create the section if not found
     */
    @NotNull
    public ConfigSection getOrCreateConfigurationSection(@NotNull String path) {
        return Objects.requireNonNull(getT(path, ConfigSection.class, createSection(path)));
    }
}
