package com.illuzionzstudios.mist.config.format;

import java.util.List;

/**
 * These are the different ways to display a {@link Comment}
 */
public enum CommentStyle {

    /**
     * # Comment
     */
    SIMPLE(false, false, " ", ""),
    /**
     * #           <br />
     * # Comment   <br />
     * #           <br />
     */
    SPACED(false, true, " ", ""),
    /**
     * ########### <br />
     * # Comment # <br />
     * ########### <br />
     */
    BLOCKED(true, false, " ", " "),
    /**
     * ############# <br />
     * #|¯¯¯¯¯¯¯¯¯|# <br />
     * #| Comment |# <br />
     * #|_________|# <br />
     * ############# <br />
     */
    BLOCKSPACED(true, true, "|\u00AF", '\u00AF', "\u00AF|", "| ", " |", "|_", '_', "_|");

    /**
     * Store the different options in order to draw the comment styling
     */
    final boolean drawBorder, drawSpace;
    final String commentPrefix, spacePrefixTop, spacePrefixBottom;
    final String commentSuffix, spaceSuffixTop, spaceSuffixBottom;
    final char spaceCharTop, spaceCharBottom;

    CommentStyle(boolean drawBorder, boolean drawSpace,
                 String spacePrefixTop, char spaceCharTop, String spaceSuffixTop,
                 String commentPrefix, String commentSuffix,
                 String spacePrefixBottom, char spaceCharBottom, String spaceSuffixBottom) {
        this.drawBorder = drawBorder;
        this.drawSpace = drawSpace;
        this.commentPrefix = commentPrefix;
        this.spacePrefixTop = spacePrefixTop;
        this.spacePrefixBottom = spacePrefixBottom;
        this.commentSuffix = commentSuffix;
        this.spaceSuffixTop = spaceSuffixTop;
        this.spaceSuffixBottom = spaceSuffixBottom;
        this.spaceCharTop = spaceCharTop;
        this.spaceCharBottom = spaceCharBottom;
    }

    CommentStyle(boolean drawBorder, boolean drawSpace, String commentPrefix, String commentSuffix) {
        this.drawBorder = drawBorder;
        this.drawSpace = drawSpace;
        this.commentPrefix = commentPrefix;
        this.commentSuffix = commentSuffix;
        this.spacePrefixTop = this.spacePrefixBottom = "";
        this.spaceCharTop = this.spaceCharBottom = ' ';
        this.spaceSuffixTop = this.spaceSuffixBottom = "";
    }

    /**
     * An easy way to detect what type of styling
     * a comment has
     *
     * @param lines The lines with styling
     * @return The relevant comment styling
     */
    public static CommentStyle parseStyle(List<String> lines) {
        if (lines == null || lines.size() <= 2) {
            return CommentStyle.SIMPLE;
        } else if (lines.size() > 2 && lines.get(0).trim().equals("#") && lines.get(lines.size() - 1).trim().equals("#")) {
            return CommentStyle.SPACED;
        }
        boolean hasBorders = lines.size() > 2 && lines.get(0).trim().matches("^##+$") && lines.get(lines.size() - 1).trim().matches("^##+$");
        if (!hasBorders) {
            // default return
            return CommentStyle.SIMPLE;
        }
        // now need to figure out if this is blocked or not
        final String replace = ("^#"
                + CommentStyle.BLOCKSPACED.spacePrefixTop + CommentStyle.BLOCKSPACED.spaceCharTop + "+"
                + CommentStyle.BLOCKSPACED.spaceSuffixTop + "#$").replace("|", "\\|");
        if (lines.size() > 4 && lines.get(1).trim().matches(replace)
                && lines.get(1).trim().matches(replace)) {
            return CommentStyle.BLOCKSPACED;
        }
        return CommentStyle.BLOCKED;
    }
}
