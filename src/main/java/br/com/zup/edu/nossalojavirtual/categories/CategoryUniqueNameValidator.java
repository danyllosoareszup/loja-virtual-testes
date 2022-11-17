package br.com.zup.edu.nossalojavirtual.categories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

class CategoryUniqueNameValidator implements Validator {

    Logger logger = LoggerFactory.getLogger(CategoryUniqueNameValidator.class);

    private final CategoryRepository categoryRepository;

    CategoryUniqueNameValidator(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return NewCategoryRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        var newCategory = (NewCategoryRequest) target;
        String name = newCategory.getName();

        if (categoryRepository.existsByName(name)) {
            logger.warn("this category is already registered");
            errors.rejectValue("name", "category.name.alreadyExists", "this category is already registered");
        }
    }
}
