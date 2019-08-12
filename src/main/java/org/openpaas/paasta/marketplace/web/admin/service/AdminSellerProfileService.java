package org.openpaas.paasta.marketplace.web.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.openpaas.paasta.marketplace.api.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminSellerProfileService {

    @Autowired
    private final RestTemplate paasApiRest;

    @SneakyThrows
    public List getProfiles() {
        Map map  = paasApiRest.getForObject("/admin/profiles/page", Map.class);
        System.out.println(map.toString());
        return null;
    }

    //Page::Profile
    public CustomPage<Profile> getProfileList(String queryParamString) {
        ResponseEntity<CustomPage<Profile>> responseEntity = paasApiRest.exchange("/admin/profiles/page" + queryParamString, HttpMethod.GET, null, new ParameterizedTypeReference<CustomPage<Profile>>() {});
        CustomPage<Profile> customPage = responseEntity.getBody();
        return customPage;

    }

    public Profile getProfiles(String id) {
        String url = UriComponentsBuilder.newInstance().path("/admin/profiles/{id}")
                .build()
                .expand(id)
                .toString();

        return paasApiRest.getForObject(url, Profile.class);
    }

    public Profile updateProfiles(String id) {
        String url = UriComponentsBuilder.newInstance().path("/admin/profiles/{id}")
                .build()
                .expand(id)
                .toString();

        return paasApiRest.getForObject(url, Profile.class);
    }




}
