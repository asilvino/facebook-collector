package models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by alvaro.joao.silvino on 20/08/2015.
 */
@Document
public class Page {

    @Id
    private String id;

    private  String title;

    public Page(){

    }

    public String getTitle(){
        return title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object obj) {
        return (this.id.equals(((Page)obj).id));
    }

    @Override
    public int hashCode() {
        return (this.id).hashCode();
    }


}

