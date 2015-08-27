package models;

import com.mongodb.DBObject;

import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.stereotype.Component;

import play.Logger;

/**
 * Created by alvaro.joao.silvino on 22/08/2015.
 */
@Component
public class BeforeSaveListener  extends AbstractMongoEventListener<User> {
    @Override
    public void onBeforeSave (User user,DBObject dbo) {
        dbo.put("likesCount", user.getLikes().size());
        dbo.put("commentsCount", user.getComments().size());
        dbo.put("pagesCount", user.getPages().size());
    }
}
