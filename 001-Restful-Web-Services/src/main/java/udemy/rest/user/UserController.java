package udemy.rest.user;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import jakarta.validation.Valid;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository users;
    private final PostRepository posts;

    public UserController(UserRepository users, PostRepository posts) {
        this.users = users;
        this.posts = posts;
    }

    @GetMapping
    public CollectionModel<EntityModel<User>> all() {
        List<EntityModel<User>> models = users.findAll().stream().map(this::toModel).toList();
        return CollectionModel.of(
                models, linkTo(methodOn(UserController.class).all()).withSelfRel());
    }

    @GetMapping("/{id}")
    public EntityModel<User> one(@PathVariable long id) {
        return toModel(find(id));
    }

    @PostMapping
    public ResponseEntity<EntityModel<User>> create(@Valid @RequestBody User user) {
        User saved = users.save(new User(user.getName(), user.getBirthDate()));
        var location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(saved.getId())
                        .toUri();
        return ResponseEntity.created(location).body(toModel(saved));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable long id) {
        User user = find(id);
        posts.deleteAll(user.getPosts());
        users.delete(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/posts")
    @Transactional(readOnly = true)
    public List<Post> postsOf(@PathVariable long id) {
        return List.copyOf(find(id).getPosts());
    }

    @PostMapping("/{id}/posts")
    public ResponseEntity<Post> createPost(@PathVariable long id, @Valid @RequestBody Post post) {
        Post created = new Post(post.getDescription());
        created.setUser(find(id));
        Post saved = posts.save(created);
        var location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{postId}")
                        .buildAndExpand(saved.getId())
                        .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    private User find(long id) {
        return users.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private EntityModel<User> toModel(User user) {
        return EntityModel.of(
                user,
                linkTo(methodOn(UserController.class).one(user.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).postsOf(user.getId())).withRel("posts"),
                linkTo(methodOn(UserController.class).all()).withRel("all-users"));
    }
}
