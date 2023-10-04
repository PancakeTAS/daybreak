package de.pancake.daybreak.webhook.data;

import lombok.Builder;

/**
 * Webhook request.
 * @author Pancale
 */
@Builder
public class Request {

    /** The content of the message. */
    public String content;

    /** The embeds of the message. */
    public Embed[] embeds;

    /** The username of the message. */
    public String username;

    /** The avatar url of the message. */
    public String avatar_url;

}
