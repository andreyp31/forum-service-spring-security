package telran.java45.post.security;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import telran.java45.post.dao.PostRepository;
import telran.java45.post.model.Post;

@Service("customSecurity")
@RequiredArgsConstructor
public class CustomWebSecurity {
  final PostRepository repository;
  
  public boolean checkPostAuthor(String postId, String userName) {
	Post post = repository.findById(postId).orElse(null);
	return post !=null && userName.equalsIgnoreCase(post.getAuthor());
}
}
