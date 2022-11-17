package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.shared.validators.ObjectIsRegisteredValidator;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.http.ResponseEntity.created;

@RestController
@RequestMapping("/api/opinions")
class ProductOpinionController {

    Logger logger = LoggerFactory.getLogger(ProductOpinionController.class);


    private final ProductOpinionRepository productOpinionRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    ProductOpinionController(ProductOpinionRepository productOpinionRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.productOpinionRepository = productOpinionRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    ResponseEntity<?> create(@RequestBody @Valid NewOpinionRequest newOpinion,
                             @AuthenticationPrincipal Jwt jwtUser
                            ) {

        logger.info("Starting product opinion {} registration", newOpinion.getTitle());

        String userEmail = jwtUser.getClaim("email");

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    logger.warn("user {} not registered", userEmail);
                    return new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "usuário não registrado");
                });

        var opinion = newOpinion.toProductOpinion(productRepository::findById, user);
        productOpinionRepository.save(opinion);

        logger.info("product opinion {} successfully registered", newOpinion.getTitle());

        URI location = URI.create("/api/opinions/" + opinion.getId());
        return created(location).build();
    }

    @InitBinder(value = { "newOpinionRequest" })
    void initBinder(WebDataBinder binder) {
        binder.addValidators(new ObjectIsRegisteredValidator<>("productId",
                "product.id.dontExist",
                NewOpinionRequest.class,
                productRepository::existsById));
    }
}
