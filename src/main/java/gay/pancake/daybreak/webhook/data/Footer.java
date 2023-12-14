package gay.pancake.daybreak.webhook.data;

import lombok.Builder;

/**
 * Footer of the webhook.
 * @author Pancake
 */
@Builder
public class Footer {

    /** Text of the footer. */
    public String text;

    /** Icon url of the footer. */
    public String icon_url;

}
