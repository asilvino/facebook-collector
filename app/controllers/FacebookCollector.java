package controllers;

import org.joda.time.DateTime;
import org.springframework.social.facebook.api.Comment;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.social.facebook.api.PagingParameters;
import org.springframework.social.facebook.api.Post;
import org.springframework.social.facebook.api.Reference;
import org.springframework.social.facebook.api.impl.FacebookTemplate;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bootstrap.CollectorInfo;
import bootstrap.DS;
import models.Page;
import models.User;
import play.Logger;
import play.Play;
import service.MongoService;

/**
 * Created by alvaro.joao.silvino on 20/08/2015.
 */
//db.user.find({$where:'this.comments.length>4'}).pretty()
//> db.user.find({$and:[{$where:'this.comments.length>4'},{$where:'this.likes.length>4'}]}).pretty()
public class FacebookCollector {

    public enum FacebookPages {
        chicorei("162726143745402"),
        king55style("177679275612961"),
        mestredalma("160540370633021"),
        camiseteria("7018060973"),
        kanuibr("197061883680740");

        public String id;
        private FacebookPages(String id){
            this.id = id;
        }
        public static List<String> getList() {
            List<String> tags = new ArrayList<>();

            for (FacebookPages tag : FacebookPages.values()) {
                tags.add(tag.name());
            }
            return tags;
        }
        public static FacebookPages getById(String id){
            for (FacebookPages tag : FacebookPages.values()) {
                if(tag.id.equals(id))
                    return tag;
            }
            return null;
        }
        public static List<String> getListId() {
            List<String> tags = new ArrayList<>();

            for (FacebookPages tag : FacebookPages.values()) {
                tags.add(tag.id);
            }
            return tags;
        }
    }


    public static String token = Play.application().configuration().getString("facebook.token");
    public static Facebook facebook = new FacebookTemplate(token);


    public static void collect(CollectorInfo.Moment moment){
        List<Page> pages = MongoService.getAllPages();
        for(Page page:pages) {
            try {

            long lastMonth = DateTime.now().minusMonths(1).toDateTime().getMillis()/1000;
            long now = DateTime.now().toDateTime().getMillis()/1000;



            PagingParameters pagingParameters = new PagingParameters(25,null,lastMonth,now);
            PagedList<Post> posts ;
            switch (moment){
                case ALL:
                    posts = facebook.feedOperations().getPosts(page.getId());
                    break;
                case RECENT:
                    posts = facebook.feedOperations().getPosts(page.getId(),pagingParameters);
                    break;
                default:
                    posts = facebook.feedOperations().getPosts(page.getId());
                    break;
            }

            boolean firstTime = true;
            do{
                try{
                    if(!firstTime)
                        posts = facebook.feedOperations().getPosts(page.getId(),posts.getNextPage());
                    firstTime = false;
                    for(Post post: posts) {
                        Set<User> users = new HashSet<>();
                        Set<Comment> comments = new HashSet<>();
                        fetchCommentAndUpdateUsers(post, comments, users, page);
                        fetchLikesAndUpdateUsers(post, users,page);

                        MongoService.save(post);

                        for(Comment comment: comments){
                            MongoService.save(comment);
                        }
                        //save or update users iterations
                        MongoService.save(users);
                    }
                }catch (Exception e){
                    Logger.debug("error on get more  posts: "+e.getMessage() );
                }
                Logger.debug("update:"+page.getTitle()+"  " );
            }while(posts.getNextPage()!=null);
            Logger.debug("Finished page:"+page.getId());

            }catch (Exception e){
                Logger.debug("error on page:"+page.getId());
            }
        }
    }

    private static void fetchLikesAndUpdateUsers(Post post, Set<User> users,Page page) {
        PagedList<Reference> likes = facebook.likeOperations().getLikes(post.getId());
        int totalLikes = likes.size();
        for(Reference userLike: likes) {
            User user = users.stream().filter(f->f.getId().equals(userLike.getId())).findFirst().orElse(new User(userLike.getId(),userLike.getName(),page));
            user.addLike(post,page.getId());
            users.add(user);
        }
        while (likes.getNextPage() != null) {
            likes = facebook.likeOperations().getLikes(post.getId(), likes.getNextPage());
            totalLikes +=likes.size();
            for(Reference userLike: likes) {
                User user = users.stream().filter(f->f.getId().equals(userLike.getId())).findFirst().orElse(new User(userLike.getId(),userLike.getName(),page));
                user.addLike(post,page.getId());
                users.add(user);
            }
        }
        post.getExtraData().putIfAbsent("likesCount",totalLikes);
    }

    private static void fetchCommentAndUpdateUsers(Post post,Set<Comment> commentsToSave, Set<User> users,Page page) {
        PagedList<Comment> comments = facebook.commentOperations().getComments(post.getId());
        int totalComments = comments.size();

        for(Comment comment: comments) {

            User user = users.stream().filter(f->f.getId().equals(comment.getFrom().getId())).findFirst().orElse(new User(comment.getFrom().getId(),comment.getFrom().getName(),page));
            user.addComment(comment, post,page.getId());
            users.add(user);


            commentsToSave.add(comment);
            if(comment.getCommentCount()!=null&&comment.getCommentCount()>0){
                PagedList<Comment> commentsComments = facebook.commentOperations().getComments(comment.getId());
                for(Comment commentsComment: commentsComments) {

                    user = users.stream().filter(f->f.getId().equals(commentsComment.getFrom().getId())).findFirst().orElse(new User(comment.getFrom().getId(),comment.getFrom().getName(),page));
                    user.addComment(commentsComment, post,page.getId());
                    users.add(user);
                    commentsToSave.add(comment);
                }
                while (commentsComments.getNextPage() != null) {
                    commentsComments = facebook.commentOperations().getComments(post.getId(), comments.getNextPage());
                    for(Comment commentsComment: commentsComments) {
                        user = users.stream().filter(f->f.getId().equals(commentsComment.getFrom().getId())).findFirst().orElse(new User(comment.getFrom().getId(),comment.getFrom().getName(),page));
                        user.addComment(commentsComment, post,page.getId());
                        users.add(user);
                        commentsToSave.add(comment);
                    }
                }

            }
        }
        while (comments.getNextPage() != null) {
            comments = facebook.commentOperations().getComments(post.getId(), comments.getNextPage());
            totalComments +=comments.size();

            for(Comment comment: comments) {
                User user = users.stream().filter(f->f.getId().equals(comment.getFrom().getId())).findFirst().orElse(new User(comment.getFrom().getId(),comment.getFrom().getName(),page));
                user.addComment(comment, post,page.getId());
                users.add(user);
                commentsToSave.add(comment);
                if(comment.getCommentCount()!=null&&comment.getCommentCount()>0){
                    PagedList<Comment> commentsComments = facebook.commentOperations().getComments(comment.getId());
                    for(Comment commentsComment: commentsComments) {

                        user = users.stream().filter(f->f.getId().equals(commentsComment.getFrom().getId())).findFirst().orElse(new User(comment.getFrom().getId(),comment.getFrom().getName(),page));
                        user.addComment(commentsComment, post,page.getId());
                        users.add(user);
                        commentsToSave.add(comment);
                    }
                    while (commentsComments.getNextPage() != null) {
                        commentsComments = facebook.commentOperations().getComments(post.getId(), comments.getNextPage());
                        for(Comment commentsComment: commentsComments) {
                            user = users.stream().filter(f->f.getId().equals(commentsComment.getFrom().getId())).findFirst().orElse(new User(comment.getFrom().getId(),comment.getFrom().getName(),page));
                            user.addComment(commentsComment, post,page.getId());
                            users.add(user);
                            commentsToSave.add(comment);
                        }
                    }
                }
            }
        }
        post.getExtraData().putIfAbsent("commentsCount",totalComments);

    }

}
