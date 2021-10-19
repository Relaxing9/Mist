package com.illuzionzstudios.mist.config;

import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.Mist;
import com.illuzionzstudios.mist.config.format.Comment;
import com.illuzionzstudios.mist.config.format.CommentStyle;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.util.FileUtil;
import com.illuzionzstudios.mist.util.TextUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles a {@link ConfigSection} as a YAML file. This means being a file
 * and having a file location along with saving, reading, and writing utils.
 */
public class YamlConfig extends ConfigSection {

    //  -------------------------------------------------------------------------
    //  Final variables
    //  -------------------------------------------------------------------------

    /**
     * The {@link String} representation of a blank {@link YamlConfig}
     */
    protected static final String BLANK_CONFIG = "{}\n";

    /**
     * This is the REGEX to parse YAML syntax. Matches "key: value" making sure syntax is right
     */
    protected final Pattern yamlNode = Pattern.compile("^( *)([^:{}\\[\\],&*#?|<>=!%@`]+):(.*)$");

    /**
     * This is the path to the directory to store the file in. This
     * is taken relative from the {@link JavaPlugin#getDataFolder()}, meaning
     * the value "" is the {@link JavaPlugin#getDataFolder()}.
     *
     * If we set this value to "foo", this final dir would be "foo/config.yml"
     */
    @Getter
    protected final String directory;

    /**
     * This is the name of the file for this {@link YamlConfig}. Name must include
     * the file extensions, otherwise default "yml" will be appended.
     */
    @Getter
    protected final String fileName;

    /**
     * This is the instance of the {@link SpigotPlugin} that this {@link YamlConfig}
     * belongs to.
     */
    @Getter
    protected final SpigotPlugin plugin;

    /**
     * These are YAML options used to help parse the file
     */
    private final DumperOptions yamlOptions = new DumperOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

    //  -------------------------------------------------------------------------
    //  Properties of the config
    //  -------------------------------------------------------------------------

    /**
     * Flag indicating if the file is loaded
     */
    @Getter
    @Setter
    protected boolean loaded = false;

    /**
     * This is the actual file that this {@link YamlConfig} is
     */
    protected File file;

    /**
     * Comments to display at the top/bottom of the file
     * to give more clarity on the contents
     */
    protected Comment headerComment;
    protected Comment footerComment;

    /**
     * This is the {@link Charset} to use for saving the file
     */
    @Getter
    @Setter
    private Charset defaultCharset = StandardCharsets.UTF_8;

    /**
     * This flag indicates if we should remove nodes not included in defaults
     */
    @Getter
    @Setter
    private boolean autoRemove = false;

    /**
     * This flag indicates if we should load comments to the file
     */
    @Getter
    @Setter
    private boolean loadComments = true;

    /**
     * Default comment styling applied to nodes that hold a normal value
     */
    @Getter
    @Setter
    private CommentStyle defaultNodeCommentFormat = CommentStyle.SIMPLE;

    /**
     * Default comment styling applied to nodes that hold a {@link ConfigSection}
     */
    @Getter
    @Setter
    private CommentStyle defaultSectionCommentFormat = CommentStyle.SPACED;

    /**
     * Extra lines to put between root nodes, as in a "\n"
     */
    @Getter
    @Setter
    private int rootNodeSpacing = 1;

    /**
     * Extra lines to put in front of comments. <br>
     * This is separate from rootNodeSpacing, if applicable.
     * These are " " characters
     */
    @Getter
    @Setter
    private int commentSpacing = 0;

    //  -------------------------------------------------------------------------
    //  Constructors
    //  -------------------------------------------------------------------------

    public YamlConfig() {
        this.plugin = null;
        this.file = null;
        directory = null;
        fileName = null;
    }

    public YamlConfig(@NotNull File file) {
        this.plugin = null;
        this.file = file.getAbsoluteFile();
        directory = null;
        fileName = file.getName();
    }

    public YamlConfig(@NotNull SpigotPlugin plugin) {
        this.plugin = plugin;
        directory = null;
        fileName = null;
    }

    public YamlConfig(@NotNull SpigotPlugin plugin, @NotNull String file) {
        this.plugin = plugin;
        directory = null;
        fileName = file;
    }

    public YamlConfig(@NotNull SpigotPlugin plugin, @Nullable String directory, @NotNull String file) {
        this.plugin = plugin;
        this.directory = directory;
        fileName = file;
    }

    /**
     * Get the {@link File} for this instance
     *
     * @return The file if directory isn't null, otherwise file with name "config.yml"
     */
    @NotNull
    public File getFile() {
        if (file == null) {
            if (directory != null) {
                this.file = new File(plugin.getDataFolder() + directory, fileName != null ? fileName : Mist.SETTINGS_NAME);
            } else {
                this.file = new File(plugin.getDataFolder(), fileName != null ? fileName : Mist.SETTINGS_NAME);
            }
        }
        return file;
    }

    /**
     * Set the {@link #headerComment} from VarArgs of {@link String}
     *
     * @param description Strings to set for comment
     */
    public void setHeader(@NotNull String... description) {
        if (description.length == 0) {
            headerComment = null;
        } else {
            headerComment = new Comment(CommentStyle.BLOCKED, description);
        }
    }

    /**
     * Set the {@link #footerComment} from VarArgs of {@link String}
     *
     * @param description Strings to set for comment
     */
    public void setFooter(@NotNull String... description) {
        if (description.length == 0) {
            footerComment = null;
        } else {
            footerComment = new Comment(CommentStyle.BLOCKED, description);
        }
    }

    /**
     * @return {@link String} lines from {@link #headerComment}
     */
    @NotNull
    public List<String> getHeader() {
        if (headerComment != null) {
            return headerComment.getLines();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * @return {@link String} lines from {@link #footerComment}
     */
    @NotNull
    public List<String> getFooter() {
        if (footerComment != null) {
            return footerComment.getLines();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Clear all nodes and values in this config in memory.
     * DOES NOT SAVE TO DISK.
     *
     * @param clearDefaults If to also invoke {@link #clearDefaults()}
     */
    public void clearConfig(boolean clearDefaults) {
        root.values.clear();
        root.configComments.clear();

        if (clearDefaults) {
            clearDefaults();
        }
    }

    /**
     * Clear all default options in the {@link ConfigSection}
     */
    public void clearDefaults() {
        root.defaultComments.clear();
        root.defaults.clear();
    }

    //  -------------------------------------------------------------------------
    //  File Loading
    //  -------------------------------------------------------------------------

    /**
     * To be overridden, called before loading the file into memory
     */
    protected void preLoad() {
    }

    /**
     * To be overridden, called after loading the file into memory
     */
    protected void postLoad() {
    }

    /**
     * Static method to just load an internal YAML file right onto the server without having
     * to create a custom class because we aren't doing any internal loading.
     *
     * @param plugin The plugin this file is apart of
     * @param directory The directory to put the file in
     * @param fileName The name of the file
     */
    public static void loadInternalYaml(final SpigotPlugin plugin, final String directory, final String fileName) {
        YamlConfig toLoad = new YamlConfig(plugin, File.separator + directory, fileName);
        toLoad.loadResourceToServer(directory, fileName);
    }

    /**
     * Loads an internal resource onto the server
     * For instance file in resources/locales/en_US.lang will be loaded
     * onto the server under plugins/MY_PLUGIN/locales/en_US.lang
     *
     * Main applications are implementing this {@link YamlConfig} into a custom
     * object, for instance implementing a specific type of config. This way if we have
     * any defaults in the plugin we can load to the server.
     *
     * If file already exists on disk it just loads that.
     *
     * @param directory The directory to load from, "" if none
     * @param fileName File name with extension to load
     * @return If was found and loaded successfully
     */
    public final boolean loadResourceToServer(final String directory, final String fileName) {
        Objects.requireNonNull(fileName, "Must provide a file to load");

        // Internal path to locale
        final String internalPath = (directory.trim().equalsIgnoreCase("") ? "" : directory + "/") + fileName;
        // Attempt to find resource
        final InputStream input = FileUtil.getInternalResource(internalPath);

        // Existing file
        final File existingFile = new File(plugin.getDataFolder(), internalPath);

        // Load buffers
        // Input stream for internal file and existing file
        try (BufferedInputStream defaultIn = new BufferedInputStream(input)) {
            try (BufferedReader defaultReader = new BufferedReader(new InputStreamReader(defaultIn))) {

                // File exists, load that
                if (existingFile.exists()) {
                    try (BufferedInputStream existingIn = new BufferedInputStream(new FileInputStream(existingFile));
                         BufferedReader existingReader = new BufferedReader(new InputStreamReader(existingIn))) {
                        load(existingReader);
                    } catch (Exception ex) {
                        Logger.displayError(ex, "File " + fileName + " exists but couldn't be loaded");
                    }
                } else {
                    // Load from default
                    load(defaultReader);
                    // Then save in server
                    save(existingFile);
                }

                return true;
            } catch (Exception ex) {
                // Couldn't find internal resource so just don't even load
            }
        } catch (Exception ex) {
            // Couldn't find internal resource so just don't even load
        }

        return false;
    }

    /**
     * Load a locale file from internal resources. If file doesn't already exist
     * on the server, we create it.
     *
     * @param locale The locale name, eg "en_US"
     */
    protected final boolean loadLocale(final String locale) {
        return loadResourceToServer("locales", locale + ".lang");
    }

    /**
     * Load the {@link #file} into memory
     *
     * @return If loaded successfully
     */
    public boolean load() {
        return load(getFile());
    }

    /**
     * See {@link #load()}
     *
     * @param file The file object to load
     */
    public boolean load(@NotNull File file) {
        Validate.notNull(file, "File cannot be null");

        // Start loading
        preLoad();
        this.loaded = true;

        if (file.exists()) {
            try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
                Charset charset = TextUtil.detectCharset(stream, StandardCharsets.UTF_8);
                // Upgrade charset if file was saved in a more complex format
                if (charset == StandardCharsets.UTF_16BE || charset == StandardCharsets.UTF_16LE) {
                    defaultCharset = StandardCharsets.UTF_16;
                }
                this.load(new InputStreamReader(stream, charset));
                return true;
            } catch (IOException | InvalidConfigurationException ex) {
                (plugin != null ? plugin.getLogger() : Bukkit.getLogger()).log(Level.SEVERE, "Failed to load config file: " + file.getName(), ex);
            }
            return false;
        }
        return true;
    }

    public void load(@NotNull Reader reader) throws IOException, InvalidConfigurationException {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {
            String line;
            boolean firstLine = true;
            while ((line = input.readLine()) != null) {
                if (firstLine) {
                    line = line.replaceAll("[\uFEFF\uFFFE\u200B]", ""); // clear BOM markers
                    firstLine = false;
                }
                builder.append(line).append('\n');
            }
        }
        this.loadFromString(builder.toString());
    }

    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        Map<?, ?> input;
        try {
            input = this.yaml.load(contents);
        } catch (YAMLException e2) {
            throw new InvalidConfigurationException(e2);
        } catch (ClassCastException e3) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }
        if (input != null) {
            if (loadComments) {
                this.parseComments(contents, input);
            }
            this.convertMapsToSections(input, this);
        }

        // Loading is done
        postLoad();
    }

    protected void convertMapsToSections(@NotNull Map<?, ?> input, @NotNull ConfigSection section) {
        for (final Map.Entry<?, ?> entry : input.entrySet()) {
            final String key = entry.getKey().toString();
            final Object value = entry.getValue();

            if (value instanceof Map) {
                convertMapsToSections((Map<?, ?>) value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }
    }

    protected void parseComments(@NotNull String contents, @NotNull Map<?, ?> input) {
        // If starts with a comment, load all non-breaking comments as a header
        // then load all comments and assign to the next valid node loaded
        // (Only load comments that are on their own line)

        BufferedReader in = new BufferedReader(new StringReader(contents));
        String line;
        boolean insideScalar = false;
        boolean firstNode = true;
        int index = 0;
        LinkedList<String> currentPath = new LinkedList<>();
        ArrayList<String> commentBlock = new ArrayList<>();
        try {
            while ((line = in.readLine()) != null) {
                if (line.isEmpty()) {
                    if (firstNode && !commentBlock.isEmpty()) {
                        // header comment
                        firstNode = false;
                        headerComment = Comment.loadComment(commentBlock);
                        commentBlock.clear();
                    }
                    continue;
                } else if (line.trim().startsWith("#")) {
                    // only load full-line comments
                    commentBlock.add(line.trim());
                    continue;
                }

                // check to see if this is a line that we can process
                int lineOffset = TextUtil.getOffset(line);
                insideScalar &= lineOffset <= index;
                Matcher m;
                if (!insideScalar && (m = yamlNode.matcher(line)).find()) {
                    // we found a config node! ^.^
                    // check to see what the full path is
                    int depth = (m.group(1).length() / indentation);
                    while (depth < currentPath.size()) {
                        currentPath.removeLast();
                    }
                    currentPath.add(m.group(2));

                    // do we have a comment for this node?
                    if (!commentBlock.isEmpty()) {
                        String path = currentPath.stream().collect(Collectors.joining(String.valueOf(pathSeparator)));
                        Comment comment = Comment.loadComment(commentBlock);
                        commentBlock.clear();
                        setComment(path, comment);
                    }

                    firstNode = false; // we're no longer on the first node

                    // ignore scalars
                    index = lineOffset;
                    if (m.group(3).trim().equals("|") || m.group(3).trim().equals(">")) {
                        insideScalar = true;
                    }
                }
            }
            if (!commentBlock.isEmpty()) {
                footerComment = Comment.loadComment(commentBlock);
                commentBlock.clear();
            }
        } catch (IOException ignored) {
        }

    }

    /**
     * Delete all nodes and values that aren't default values
     */
    public void deleteNonDefaultSettings() {
        // Delete old config values (thread-safe)
        List<String> defaultKeys = Arrays.asList(defaults.keySet().toArray(new String[0]));
        for (String key : values.keySet().toArray(new String[0])) {
            if (!defaultKeys.contains(key)) {
                values.remove(key);
            }
        }
    }

    /**
     * Every time we change the file through code save it to disk
     */
    @Override
    protected void onChange() {
        saveChanges();
    }

    /**
     * Save current values in memory to the file on disk
     *
     * @return If it saved correctly
     */
    public boolean saveChanges() {
        boolean saved = true;
        if (changed || hasNewDefaults()) {
            saved = save();
        }

        return saved;
    }

    /**
     * @return If has default values defined by no values
     * have been set at those nodes.
     */
    public boolean hasNewDefaults() {
        if (file != null && !file.exists()) return true;
        for (String def : defaults.keySet()) {
            if (!values.containsKey(def)) return true;
        }
        return false;
    }

    public boolean save() {
        return save(getFile());
    }

    public boolean save(@NotNull String file) {
        Validate.notNull(file, "File cannot be null");
        return this.save(new File(file));
    }

    public boolean save(@NotNull File file) {
        Validate.notNull(file, "File cannot be null");
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        String data = this.saveToString();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), defaultCharset);) {
            writer.write(data);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @NotNull
    public String saveToString() {
        try {
            if (autoRemove) {
                deleteNonDefaultSettings();
            }

            yamlOptions.setIndent(indentation);
            yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            yamlOptions.setSplitLines(false);
            yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            StringWriter str = new StringWriter();
            if (headerComment != null) {
                headerComment.writeComment(str, 0, CommentStyle.BLOCKED);
                str.write("\n"); // add one space after the header
            }
            String dump = yaml.dump(this.getValues(false));
            if (!dump.equals(BLANK_CONFIG)) {
                writeComments(dump, str);
            }
            if (footerComment != null) {
                str.write("\n");
                footerComment.writeComment(str, 0, CommentStyle.BLOCKED);
            }
            return str.toString();
        } catch (Throwable ex) {
            Logger.displayError(ex, "Error loading config");
            saveChanges();
        }
        return "";
    }

    protected void writeComments(String data, Writer out) throws IOException {
        // line-by-line apply line spacing formatting and comments per-node
        BufferedReader in = new BufferedReader(new StringReader(data));
        String line;
        boolean insideScalar = false;
        boolean firstNode = true;
        int index = 0;
        LinkedList<String> currentPath = new LinkedList<>();
        while ((line = in.readLine()) != null) {
            // ignore comments and empty lines (there shouldn't be any, but just in case)
            if (line.trim().startsWith("#") || line.isEmpty()) {
                continue;
            }

            // check to see if this is a line that we can process
            int lineOffset = TextUtil.getOffset(line);
            insideScalar &= lineOffset <= index;
            Matcher m;
            if (!insideScalar && (m = yamlNode.matcher(line)).find()) {
                // we found a config node! ^.^
                // check to see what the full path is
                int depth = (m.group(1).length() / indentation);
                while (depth < currentPath.size()) {
                    currentPath.removeLast();
                }
                currentPath.add(m.group(2));
                String path = currentPath.stream().collect(Collectors.joining(String.valueOf(pathSeparator)));

                // if this is a root-level node, apply extra spacing if we aren't the first node
                if (!firstNode && depth == 0 && rootNodeSpacing > 0) {
                    out.write((new String(new char[rootNodeSpacing])).replace("\0", "\n")); // yes it's silly, but it works :>
                }
                firstNode = false; // we're no longer on the first node

                // insert the relavant comment
                Comment comment = getComment(path);
                if (comment != null) {
                    // add spacing between previous nodes and comments
                    if (depth != 0) {
                        out.write((new String(new char[commentSpacing])).replace("\0", "\n"));
                    }

                    // formatting style for this node
                    CommentStyle style = comment.getStyling();
                    if (style == null) {
                        // check to see what type of node this is
                        if (!m.group(3).trim().isEmpty()) {
                            // setting node
                            style = defaultNodeCommentFormat;
                        } else {
                            // probably a section? (need to peek ahead to check if this is a list)
                            in.mark(1000);
                            String nextLine = in.readLine().trim();
                            in.reset();
                            if (nextLine.startsWith("-")) {
                                // not a section :P
                                style = defaultNodeCommentFormat;
                            } else {
                                style = defaultSectionCommentFormat;
                            }
                        }
                    }

                    // write it down!
                    comment.writeComment(out, lineOffset, style);
                }
                // ignore scalars
                index = lineOffset;
                if (m.group(3).trim().equals("|") || m.group(3).trim().equals(">")) {
                    insideScalar = true;
                }
            }

            out.write(line);
            out.write("\n");
        }
    }

}
