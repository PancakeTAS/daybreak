package gay.pancake.daybreak.webhook.data;

import lombok.Builder;

/**
 * Field of the webhook.
 * @author Pancake
 */
@Builder
public class Field {

    /** Name of the field. */
    public String name;

    /** Value of the field. */
    public String value;

    /** Is the field inline? */
    public boolean inline;

}
