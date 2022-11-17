package br.com.zup.edu.nossalojavirtual.categories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

class SuperCategoryExistsValidator implements Validator {

    Logger logger = LoggerFactory.getLogger(SuperCategoryExistsValidator.class);

    private final CategoryRepository categoryRepository;

    SuperCategoryExistsValidator(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return NewCategoryRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        var newCategory = (NewCategoryRequest) target;
        Optional<Long> superCategory = newCategory.getSuperCategory();

        if (superCategory.isPresent()) {
            Long superCategoryId = superCategory.get();

            if (!categoryRepository.existsById(superCategoryId)) {
                logger.warn("The category informed does not exists");
                errors.rejectValue("superCategory", "category.superCategory", "The super category does not exists");
            }
        }

    }
}
