
package com.fandomhub.mongoangular.resources;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fandomhub.mongoangular.model.ForumsDTO;
import com.fandomhub.mongoangular.model.PostsDTO;
import com.fandomhub.mongoangular.model.UserDTO;
import com.fandomhub.mongoangular.repository.CommentsRepository;
import com.fandomhub.mongoangular.repository.ForumsRepository;
import com.fandomhub.mongoangular.repository.PostsRepository;
import com.fandomhub.mongoangular.repository.UserRepository;

@RestController
@CrossOrigin(origins="http://localhost:4200")
@RequestMapping("/myforumapi")
public class MyforumController {
	@Autowired
	private ForumsRepository forumsrepo;
	
	@Autowired
	private UserRepository userrepo;
	
	@Autowired
	private PostsRepository postsrepo;
	
	@Autowired
	private CommentsRepository commentsrepo;
	
	@DeleteMapping("/deleteforum/{id}")
	public String deleteForum(@PathVariable int id) {
		Optional<ForumsDTO> forum = forumsrepo.findById(id);
		if (forum.isPresent()) {
			ForumsDTO forumnow = forum.get();
			Set<Integer> followers = forumnow.getFollowers();
			List<UserDTO> users = userrepo.findAllById(followers);
			Set<Integer> posts = forumnow.getPosts();
			deletePosts(posts);
			for (UserDTO user: users) {
				List<Integer> following = user.getFollowingForums();
				following.removeAll(Arrays.asList(forumnow.getForumid()));
				user.setFollowingForums(following);
				userrepo.save(user);
			}
			forumsrepo.deleteById(id);
			return "Deleted Forum with id: " + id;
		}
		return "Could not delete Forum. Try again later.";
	}
	
	@DeleteMapping("/deletepost/{id}")
	public String deletePost(@PathVariable int id) {
		commentsrepo.deleteByPostid(id);
		Optional<PostsDTO> post = postsrepo.findById(id);
		if (post.isPresent()) {
			PostsDTO postnow = post.get();
			Optional<ForumsDTO> forum = forumsrepo.findById(postnow.getForumid());
			if (forum.isPresent()) {
				ForumsDTO forumnow = forum.get();
				Set<Integer> posts = forumnow.getPosts();
				posts.removeAll((Arrays.asList(id)));
				forumsrepo.save(forumnow);
			}
		}
		postsrepo.deleteById(id);
		return "Deleted Post with id: " + id;
	}
	
	public String deletePosts(Set<Integer> ids) {
		for (int id: ids) {
			commentsrepo.deleteByPostid(id);
			Optional<PostsDTO> post = postsrepo.findById(id);
			if (post.isPresent()) {
				PostsDTO postnow = post.get();
				Optional<ForumsDTO> forum = forumsrepo.findById(postnow.getForumid());
				if (forum.isPresent()) {
					ForumsDTO forumnow = forum.get();
					Set<Integer> posts = forumnow.getPosts();
					posts.removeAll((Arrays.asList(id)));
					forumsrepo.save(forumnow);
				}
			}
			postsrepo.deleteById(id);
		}
		
		return "Deleted Posts";
	}
}
