package br.com.zup.edu.nossalojavirtual.products;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/products/{id}")
class ProductDetailsController {

    Logger logger = LoggerFactory.getLogger(ProductDetailsController.class);

    private final ProductRepository productRepository;

    ProductDetailsController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    ResponseEntity<?> get(@PathVariable("id") UUID id, UriComponentsBuilder uriComponentsBuilder) {

        logger.info("Get product {} details", id);

        Optional<Product> possibleProduct = productRepository.findById(id);
        if (possibleProduct.isEmpty()) {
            logger.info("product {} does not exist", id);
            return notFound().build();
        }

        logger.info("Search of product details {} successful", id);

        return ok(new ProductDetailsResponse(possibleProduct.get(), uriComponentsBuilder));

    }

}
