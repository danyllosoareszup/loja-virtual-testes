package br.com.zup.edu.nossalojavirtual.categories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findCategoryById(long id);

    Category save(Category category);

    boolean existsByName(String name);

    boolean existsById(Long id);
}
