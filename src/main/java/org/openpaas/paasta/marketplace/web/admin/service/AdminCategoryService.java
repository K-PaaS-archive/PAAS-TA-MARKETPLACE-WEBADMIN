package org.openpaas.paasta.marketplace.web.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openpaas.paasta.marketplace.api.domain.Category;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCategoryService {
    private final RestTemplate paasApiRest;

    public List<Category> getCategoryList() {
        return paasApiRest.getForObject("/admin/categories?sort=seq,asc", List.class);
    }

    public Category getCategory(Long id) {
        String url = UriComponentsBuilder.newInstance().path("/admin/categories/{id}")
                .build()
                .expand(id)
                .toString();

        return paasApiRest.getForObject(url, Category.class);
    }

    public Category updateCategory(Long id, Category category) {
        String url = UriComponentsBuilder.newInstance().path("/admin/categories/{id}")
                .build()
                .expand(id)
                .toString();
        paasApiRest.put(url, category);

        return getCategory(id);
    }


    public Category createCategory(Category category) {
        return paasApiRest.postForObject("/admin/categories", category, Category.class);
    }

    public void deleteCategory(Long id) {
        paasApiRest.delete("/admin/categories/" + id);
    }

    public Category updateSeq(Long id, Category.Direction direction) {
        String url = UriComponentsBuilder.newInstance().path("/admin/categories/{id}/{direction}")
                .build()
                .expand(id, direction)
                .toString();
        paasApiRest.put(url, null);

        return getCategory(id);
    }
}
