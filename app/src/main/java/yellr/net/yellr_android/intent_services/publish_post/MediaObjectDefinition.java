package yellr.net.yellr_android.intent_services.publish_post;

/**
 * Created by TDuffy on 2/3/2015.
 */
public class MediaObjectDefinition {

    public String mediaType;

    // we defaults these since they are optional based on mediaType
    public String mediaFilename = "";
    public String mediaText = "";
    public String mediaCaption = "";

}
