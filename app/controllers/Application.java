package controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DBObject;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.data.domain.Sort;
import org.springframework.social.facebook.api.Comment;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.social.facebook.api.Post;
import org.springframework.social.facebook.api.Reference;
import org.springframework.social.facebook.api.impl.FacebookTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import bootstrap.DS;
import models.Page;
import models.User;
import play.*;
import play.libs.Json;
import play.mvc.*;

import service.MongoService;
import views.html.*;

public class Application extends Controller {


    public Result index() {
        return ok(index.render("Your new application is ready."));
    }
    public Result getUsers(){
        String page = request().getQueryString("page")!=null?request().getQueryString("page"):"1";
        String order = request().getQueryString("order")!=null?request().getQueryString("order"):"likesCount";
        String direction = request().getQueryString("direction")!=null?request().getQueryString("direction"):"desc";
        String date = request().getQueryString("date");
        String pagesIds = request().getQueryString("pages");
        String[] keyword = request().getQueryString("keyword")!=null&&!request().getQueryString("keyword").equals("")?request().getQueryString("keyword").split(" "):null;
        List<User> users ;
        long count;
        try {
            int pageInt = Integer.parseInt(page)>=1?Integer.parseInt(page):1;
            Sort.Direction direction1 = Sort.Direction.fromStringOrNull(direction.toUpperCase());
            MongoService.OrderBy orderBy = MongoService.OrderBy.valueOf(order);
            List<String> pages = null;
            if(pagesIds!=null&&!pagesIds.equals("")) {
                 pages = Arrays.asList(pagesIds.split(","));
            }
            DateTime initDateTime = null;
            DateTime endDateTime = null;

            if(date!=null&&!date.equals("")) {
                List<String> dates= Arrays.asList(date.trim().split("-"));
                DateTimeFormatter f = DateTimeFormat.forPattern("dd/MM/yyyy");
                initDateTime = f.parseDateTime(dates.get(0).trim());
                endDateTime = f.parseDateTime(dates.get(1).trim());
            }
            users = MongoService.getUsers(pageInt, direction1, orderBy,pages,initDateTime,endDateTime,keyword);
            count  = MongoService.countUsers(direction1, orderBy,pages,initDateTime,endDateTime,keyword);
        }catch (Exception e){
            users = MongoService.getUsers(1, Sort.Direction.DESC, MongoService.OrderBy.likesCount,null,null,null,null);
            count  = MongoService.countUsers(Sort.Direction.DESC, MongoService.OrderBy.likesCount,null,null,null,null);
        }

        ObjectNode object =Json.newObject();
        object.put("total",count);
        object.put("users",Json.toJson(users));
        return ok(object);
    }

    public Result getSingleUser(String id){
        User user = null;
        if(id!=null){
            user = MongoService.getUserById(id);
            Set<User.Like> newLikes = new LinkedHashSet<>();
            Set<User.Comment> newComments = new LinkedHashSet<>();
            user.getLikes().stream().sorted((l1, l2) -> {
                return -1*l1.createdDate.compareTo(l2.createdDate);
            }).forEach(e->newLikes.add(e));
            user.setLikes(newLikes);

            user.getComments().stream().sorted((l1, l2) -> {
                return -1*l1.createdDate.compareTo(l2.createdDate);
            }).forEach(e->newComments.add(e));
            user.setComments(newComments);
        }
        if(user==null){
            ObjectNode result = Json.newObject();
            result.put("err","not foun");
            return notFound(result);
        }
        return ok(Json.toJson(user));
    }

    public Result getPosts(){
        String page = request().getQueryString("page")!=null?request().getQueryString("page"):"1";
        String order = request().getQueryString("order")!=null?request().getQueryString("order"):"likesCount";
        String direction = request().getQueryString("direction")!=null?request().getQueryString("direction"):"desc";
        String date = request().getQueryString("date");
        String pagesIds = request().getQueryString("pages");
        String[] keyword = request().getQueryString("keyword")!=null&&!request().getQueryString("keyword").equals("")?request().getQueryString("keyword").split(" "):null;
        List<DBObject> posts ;
        long count;
        try {
            int pageInt = Integer.parseInt(page)>=1?Integer.parseInt(page):1;
            Sort.Direction direction1 = Sort.Direction.fromStringOrNull(direction.toUpperCase());
            MongoService.OrderBy orderBy = MongoService.OrderBy.valueOf(order);
            List<String> pages = null;
            if(pagesIds!=null&&!pagesIds.equals("")) {
                 pages = Arrays.asList(pagesIds.split(","));
            }
            DateTime initDateTime = null;
            DateTime endDateTime = null;

            if(date!=null) {
                List<String> dates= Arrays.asList(date.trim().split("-"));
                DateTimeFormatter f = DateTimeFormat.forPattern("dd/MM/yyyy");
                initDateTime = f.parseDateTime(dates.get(0).trim());
                endDateTime = f.parseDateTime(dates.get(1).trim());
            }
            posts = MongoService.getPosts(pageInt, direction1, orderBy, pages, initDateTime, endDateTime, keyword);
            count  = MongoService.countPosts(direction1, orderBy,pages,initDateTime,endDateTime,keyword);
        }catch (Exception e){
            posts = MongoService.getPosts(1, Sort.Direction.DESC, MongoService.OrderBy.likesCount, null, null, null, null);
            count  = MongoService.countPosts(Sort.Direction.DESC, MongoService.OrderBy.likesCount, null, null, null, null);
        }

        ObjectNode object =Json.newObject();
        object.put("total",count);
        object.put("posts",Json.toJson(posts));
        return ok(object);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result savePage(){
        JsonNode json = request().body().asJson();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ObjectNode result = Json.newObject();
        try {
            Page page = mapper.readValue(Json.stringify(json), Page.class);
            result.put("err", !MongoService.save(page));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ok(result);
    }
    public Result AllPage(){
        List<Page> pageList = MongoService.getAllPages();
        return ok(Json.toJson(pageList));
    }
    public Result deletePage(String id){
        if(id!=null&&!id.equals("")) {
            ObjectNode result = Json.newObject();
            result.put("err", !MongoService.deletePageById(id));
            return ok(result);
        }else{
            ObjectNode result = Json.newObject();
            result.put("err", true);
            return ok(result);
        }
    }



    public Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("jsRoutes"
                        , routes.javascript.Application.getUsers()
                        , routes.javascript.Application.getSingleUser()
                        , routes.javascript.Application.AllPage()
                        , routes.javascript.Application.savePage()
                        , routes.javascript.Application.deletePage()
                        //,controllers.routes.javascript.Projects.addGroup()
                )
        );
    }


}
