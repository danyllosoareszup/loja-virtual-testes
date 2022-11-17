package br.com.zup.edu.nossalojavirtual.categories;

import br.com.zup.edu.nossalojavirtual.shared.validators.UniqueFieldValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/categories")
class CategoryController {

    Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryRepository categoryRepository;

    CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostMapping
    ResponseEntity<?> createCategory(@RequestBody @Valid NewCategoryRequest newCategory) {

        logger.info("Starting category {} registration", newCategory.getName());

        Category category = newCategory.toCategory(categoryRepository::findCategoryById);

        categoryRepository.save(category);

        logger.info("category {} successfully registered", newCategory.getName());

        URI location = URI.create("/api/categories/" + category.getId());
        return ResponseEntity.created(location).build();
     }

    @InitBinder(value = { "newCategoryRequest" })
    void initBinder(WebDataBinder binder) {

        binder.addValidators(new UniqueFieldValidator<>("name",
                                                       "category.name.alreadyExists",
                                                        NewCategoryRequest.class,
                                                        categoryRepository::existsByName),
                             new SuperCategoryExistsValidator(categoryRepository));
    }
}
