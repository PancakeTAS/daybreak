package gay.pancake.daybreak.webhook.data;

import lombok.Builder;

/**
 * Author of the webhook.
 * @author Pancake
 */
@Builder
public class Author {

    /** Name of the author. */
    public String name;

    /** Url of the author icon. */
    public String icon_url;

    /** Link of the author. */
    public String url;

}
