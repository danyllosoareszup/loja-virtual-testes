package br.com.zup.edu.nossalojavirtual.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

interface ProductOpinionRepository extends JpaRepository<ProductOpinion, Long> {

    ProductOpinion save(ProductOpinion productOpinion);
}
