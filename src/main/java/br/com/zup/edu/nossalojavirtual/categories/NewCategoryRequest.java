package br.com.zup.edu.nossalojavirtual.categories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Objects.isNull;

class NewCategoryRequest {

    Logger logger = LoggerFactory.getLogger(NewCategoryRequest.class);

    @NotEmpty
    private String name;

    @Min(value = 1)
    private Long superCategory;

    /**
     * @deprecated frameworks eyes only
     */
    @Deprecated
    NewCategoryRequest() { }

    NewCategoryRequest(@NotEmpty String name,
                       @Min(value = 1) Long superCategory) {
        this.name = name;
        this.superCategory = superCategory;
    }


    public String getName() {
        return name;
    }

    public Optional<Long> getSuperCategory() {
        return Optional.ofNullable(superCategory);
    }

    Category toCategory(Function<Long, Optional<Category>> findCategoryById) {
        if (!isNull(superCategory)) {
            var category  = findCategoryById.apply(superCategory)
                                            .orElseThrow(() -> {
                                                logger.error("The category {} informed does not exists", superCategory);
                                                return new IllegalStateException(format("The category %s informed does not exists", superCategory));
                                            });

            return new Category(name, category);
        }

        return new Category(name);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NewCategoryRequest.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("superCategory=" + superCategory)
                .toString();
    }


}
