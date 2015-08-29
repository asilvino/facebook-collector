package service;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;

import controllers.FacebookCollector;
import play.Logger;

import bootstrap.DS;
import models.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.social.facebook.api.Comment;
import org.springframework.social.facebook.api.Post;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
/**
 * Created by alvaro.joao.silvino on 20/08/2015.
 */
public class MongoService {

    public static List<Page> getAllPages(){
        List<Page> pages = DS.mop.findAll(Page.class);
        return pages;
    }

    public static User getUserById(String id){
        return DS.mop.findById(id,User.class);
    }

    public static boolean save(Set<User> users){
        try{
            List<String> ids = users.stream().map(f->f.getId()).collect(Collectors.toList());
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").in(ids));
            List<User> usersFromDb = DS.mop.find(query,User.class);
            for(User user: users){
                User userFromDb = usersFromDb.stream().filter(f->f.getId().equals(user.getId())).findFirst().orElse(null);
                if(userFromDb!=null){
                    userFromDb.getComments().addAll(user.getComments());
                    userFromDb.getLikes().addAll(user.getLikes());
                    userFromDb.getPages().addAll(user.getPages());
                    DS.mop.save(userFromDb);
                }else{

                    DS.mop.save(user);
                }
            }
        }catch (Exception e){
            Logger.error(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    public static boolean save(Comment comment){
        try{
            DS.mop.save(comment);
        }catch (Exception e){
            Logger.error(e.getLocalizedMessage());
            return false;
        }
        return true;
    }
    public static boolean save(Post post){
        try{
            DS.mop.save(post);
        }catch (Exception e){
            Logger.error(e.getLocalizedMessage());
            return false;
        }
        return true;
    }
    public static boolean save(User user){
        try{
            DS.mop.save(user);
        }catch (Exception e){
            Logger.error(e.getLocalizedMessage());
            return false;
        }
        return true;
    }

    public static boolean save(Page page){
        try{
            DS.mop.save(page);
        }catch (Exception e){
            Logger.error(e.getLocalizedMessage());
            return false;
        }
        return true;
    }
    public static boolean deletePageById(String id){
        try{
            DS.mop.remove(findPageById(id));
        }catch (Exception e){
            Logger.error(e.getLocalizedMessage());
            return false;
        }
        return true;

    }
    public static Page findPageById(String id){
        try{
            return DS.mop.findById(id,Page.class);
        }catch (Exception e){
            Logger.error(e.getLocalizedMessage());
            return null;
        }
    }
    public enum OrderBy{
        likesCount,commentsCount;
    }

    public static List<String> getPostByKeyword(String[] keyword){
        TextCriteria criteria = TextCriteria.forDefaultLanguage()
                .matchingAny(keyword);
        Query query = TextQuery.queryText(criteria)
                .sortByScore();
        query.fields().include("_id");
        return DS.mop.find(query,DBObject.class,"post").stream().map(f->(String)f.get("_id")).collect(Collectors.toList());
    }

    public static List<User> getUsers(int page, Sort.Direction direction, OrderBy order,List<String> pages,DateTime initDateTime,DateTime endDateTime,String[] searchPost) {
        Query query = new Query();
        query.limit(25);
        query.skip((page-1)*25);
        if(pages!=null){
            List<String> pagesTitle = new ArrayList<>();
            for (String id : pages) {
                try{
                    pagesTitle.add(FacebookCollector.FacebookPages.getById(id).name());
                }catch (Exception e){

                }
            }
            query.addCriteria(Criteria.where("pages.title").all(pagesTitle));
        }else{
            query.addCriteria(Criteria.where("pages.title").in(FacebookCollector.FacebookPages.getList()));
        }

        if(initDateTime!=null&&endDateTime!=null&&searchPost!=null) {
            List<String> postIds = getPostByKeyword(searchPost);
            query.addCriteria(Criteria.where(null).andOperator(
                    Criteria.where(null).orOperator(Criteria.where("likes.postId").in(postIds),(Criteria.where("comments.postId").in(postIds))),
                    Criteria.where(null).orOperator(Criteria.where("likes.createdDate").lte(endDateTime.toDate()).gte(initDateTime.toDate()),
                    Criteria.where("comments.createdDate").lte(endDateTime.toDate()).gte(initDateTime.toDate()))));
        }else{
            if(initDateTime!=null&&endDateTime!=null){
                query.addCriteria(Criteria.where(null).orOperator(Criteria.where("likes.createdDate").lte(endDateTime.toDate()).gte(initDateTime.toDate()),
                        Criteria.where("comments.createdDate").lte(endDateTime.toDate()).gte(initDateTime.toDate())));
            }
            if(searchPost!=null){
                List<String> postIds = getPostByKeyword(searchPost);
                query.addCriteria(Criteria.where(null).orOperator(Criteria.where("likes.postId").in(postIds),(Criteria.where("comments.postId").in(postIds))));
            }
        }
        query.with(new Sort(direction,order.name()));
        query.fields().exclude("likes");
        return DS.mop.find(query,User.class);
    }

    public static long countUsers(Sort.Direction direction, OrderBy order,List<String> pages,DateTime initDateTime,DateTime endDateTime,String[] searchPost){
        Query query = new Query();
        
        if(pages!=null){
            List<String> pagesTitle = new ArrayList<>();
            for (String id : pages) {
                try{
                    pagesTitle.add(FacebookCollector.FacebookPages.getById(id).name());
                }catch (Exception e){

                }
            }
            query.addCriteria(Criteria.where("pages.title").all(pagesTitle));
        }else{
            query.addCriteria(Criteria.where("pages.title").in(FacebookCollector.FacebookPages.getList()));
        }

        if(initDateTime!=null&&endDateTime!=null&&searchPost!=null) {
            List<String> postIds = getPostByKeyword(searchPost);
            query.addCriteria(Criteria.where(null).andOperator(
                    Criteria.where(null).orOperator(Criteria.where("likes.postId").in(postIds),(Criteria.where("comments.postId").in(postIds))),
                    Criteria.where(null).orOperator(Criteria.where("likes.createdDate").lte(endDateTime.toDate()).gte(initDateTime.toDate()),
                            Criteria.where("comments.createdDate").lte(endDateTime.toDate()).gte(initDateTime.toDate()))));
        }else{
            if(initDateTime!=null&&endDateTime!=null){
                query.addCriteria(Criteria.where(null).orOperator(Criteria.where("likes.createdDate").lte(endDateTime.toDate()).gte(initDateTime.toDate()),
                        Criteria.where("comments.createdDate").lte(endDateTime.toDate()).gte(initDateTime.toDate())));
            }
            if(searchPost!=null){
                List<String> postIds = getPostByKeyword(searchPost);
                query.addCriteria(Criteria.where(null).orOperator(Criteria.where("likes.postId").in(postIds),(Criteria.where("comments.postId").in(postIds))));
            }
        }
        query.with(new Sort(direction,order.name()));
        query.fields().exclude("likes");
        return DS.mop.count(query,User.class);
    }

    public static List<DBObject> getPosts(int page, Sort.Direction direction, OrderBy order,List<String> pages,DateTime initDateTime,DateTime endDateTime,String[] keyword) {
        Query query = new Query();
        if(keyword!=null){
            TextCriteria criteria = TextCriteria.forDefaultLanguage()
                .matchingAny(keyword);
            query = TextQuery.queryText(criteria)
                .sortByScore();
        }

        query.limit(25);
        query.skip((page-1)*25);
        query.with(new Sort(direction,"extraData."+order.name()));
        query.fields().exclude("actions");
        query.fields().exclude("privacy");
        query.fields().exclude("properties");
        query.fields().exclude("extraData.likes");
        query.fields().exclude("extraData.comments");

        if(pages!=null){
            List<String> pageIds = new ArrayList<>();
            for (String title : pages) {
                try{
                    pageIds.add(FacebookCollector.FacebookPages.valueOf(title).id);
                }catch (Exception e){

                }
            }
            query.addCriteria(Criteria.where("from._id").all(pages));
        }else{
            query.addCriteria(Criteria.where("from._id").in(FacebookCollector.FacebookPages.getListId()));
        }
        if(initDateTime!=null&&endDateTime!=null){
            query.addCriteria(Criteria.where("createdTime").lte(endDateTime.toDate()).gte(initDateTime.toDate()));
        }
        
        return DS.mop.find(query,DBObject.class,"post");
    }

    public static long countPosts(Sort.Direction direction, OrderBy order,List<String> pages,DateTime initDateTime,DateTime endDateTime,String[] keyword){
        Query query = new Query();
        if(keyword!=null){
            TextCriteria criteria = TextCriteria.forDefaultLanguage()
                    .matchingAny(keyword);
            query = TextQuery.queryText(criteria)
                    .sortByScore();
        }
        query.with(new Sort(direction,"extraData."+order.name()));
        if(pages!=null){
            
            query.addCriteria(Criteria.where("from._id").all(pages));
        }else{
            query.addCriteria(Criteria.where("from._id").in(FacebookCollector.FacebookPages.getListId()));
        }
        if(initDateTime!=null&&endDateTime!=null){
            query.addCriteria(Criteria.where("createdTime").lte(endDateTime.toDate()).gte(initDateTime.toDate()));
        }
        return DS.mop.count(query,Post.class);
    }
}