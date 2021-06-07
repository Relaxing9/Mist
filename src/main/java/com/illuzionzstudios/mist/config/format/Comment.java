package com.illuzionzstudios.mist.config.format;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This represents a comment in a {@link com.illuzionzstudios.mist.config.YamlConfig}
 * Usually declared by the "#" char
 */
@NoArgsConstructor
public class Comment {

    /**
     * A list of strings to display for the comment
     */
    @Getter
    private final List<String> lines = new ArrayList<>();

    /**
     * {@link CommentStyle} to use for this comment instance
     */
    @Getter
    @Setter
    private CommentStyle styling = null;

    //  -------------------------------------------------------------------------
    //  Construct our comment
    //  -------------------------------------------------------------------------

    public Comment(String... lines) {
        this(null, Arrays.asList(lines));
    }

    public Comment(List<String> lines) {
        this(null, lines);
    }

    public Comment(CommentStyle commentStyle, String... lines) {
        this(commentStyle, Arrays.asList(lines));
    }

    public Comment(CommentStyle commentStyle, List<String> lines) {
        this.styling = commentStyle;
        if (lines != null) {
            lines.forEach(s -> this.lines.addAll(Arrays.asList(s.split("\n"))));
        }
    }

    /**
     * This will load a set of {@link String} lines into a {@link Comment} object.
     * Will automatically detect the comment styling so build the right comment
     *
     * @param lines The string lines including styling
     * @return The built {@link Comment} object
     */
    public static Comment loadComment(List<String> lines) {
        CommentStyle style = CommentStyle.parseStyle(lines);
        int linePad = (style.drawBorder ? 1 : 0) + (style.drawSpace ? 1 : 0);
        int prefix = style.commentPrefix.length();
        int suffix = style.commentSuffix.length();
        return new Comment(style, lines.subList(linePad, lines.size() - linePad).stream().map(s -> s.substring(prefix, s.length() - suffix).trim()).collect(Collectors.toList()));
    }

    /**
     * @return Convert comment to lines separated by <char>"\n"</char>
     */
    @Override
    public String toString() {
        return lines.isEmpty() ? "" : lines.stream().collect(Collectors.joining("\n"));
    }

    /**
     * Write our comments to a writer
     *
     * @param output The writer to output to
     * @param offset The offset amount of chars indent
     * @param defaultStyle The default styling for comments
     * @throws IOException If couldn't write comments
     */
    public void writeComment(Writer output, int offset, CommentStyle defaultStyle) throws IOException {
        CommentStyle style = styling != null ? styling : defaultStyle;
        int minSpacing = 0, borderSpacing = 0;
        // first draw the top of the comment
        if (style.drawBorder) {
            // grab the longest line in the list of lines
            minSpacing = lines.stream().max((s1, s2) -> s1.length() - s2.length()).get().length();
            borderSpacing = minSpacing + style.commentPrefix.length() + style.commentSuffix.length();
            // draw the first line
            output.write((new String(new char[offset])).replace('\0', ' ') + (new String(new char[borderSpacing + 2])).replace('\0', '#') + "\n");
            if (style.drawSpace) {
                output.write((new String(new char[offset])).replace('\0', ' ')
                        + "#" + style.spacePrefixTop
                        + (new String(new char[borderSpacing - style.spacePrefixTop.length() - style.spaceSuffixTop.length()])).replace('\0', style.spaceCharTop)
                        + style.spaceSuffixTop + "#\n");
            }
        } else if (style.drawSpace) {
            output.write((new String(new char[offset])).replace('\0', ' ') + "#\n");
        }
        // then the actual comment lines
        for (String line : lines) {
            // todo? should we auto-wrap comment lines that are longer than 80 characters?
            output.write((new String(new char[offset])).replace('\0', ' ') + "#" + style.commentPrefix
                    + (minSpacing == 0 ? line : line + (new String(new char[minSpacing - line.length()])).replace('\0', ' ')) + style.commentSuffix + (style.drawBorder ? "#\n" : "\n"));
        }
        // now draw the bottom of the comment border
        if (style.drawBorder) {
            if (style.drawSpace) {
                output.write((new String(new char[offset])).replace('\0', ' ')
                        + "#" + style.spacePrefixBottom
                        + (new String(new char[borderSpacing - style.spacePrefixBottom.length() - style.spaceSuffixBottom.length()])).replace('\0', style.spaceCharBottom)
                        + style.spaceSuffixBottom + "#\n");
            }
            output.write((new String(new char[offset])).replace('\0', ' ') + (new String(new char[borderSpacing + 2])).replace('\0', '#') + "\n");
        } else if (style.drawSpace) {
            output.write((new String(new char[offset])).replace('\0', ' ') + "#\n");
        }
    }
}
