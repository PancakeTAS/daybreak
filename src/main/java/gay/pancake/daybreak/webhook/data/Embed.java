package gay.pancake.daybreak.webhook.data;

import lombok.Builder;

/**
 * Embed of the webhook.
 * @author Pancake
 */
@Builder
public class Embed {

    /** The title of the embed. */
    public String title;

    /** The description of the embed. */
    public String description;

    /** The url of the embed. */
    public String url;

    /** The color of the embed. */
    public int color;

    /** The fields of the embed. */
    public Field[] fields;

    /** The author of the embed. */
    public Author author;

    /** The footer of the embed. */
    public Footer footer;

    /** The timestamp of the embed. */
    public String timestamp;

    /** The image of the embed. */
    public Image image;

    /** The thumbnail of the embed. */
    public Image thumbnail;

}
