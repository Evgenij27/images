package com.agile.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ImageFetcher {

    private static final String IMAGES_URI = "http://interview.agileengine.com/images";

    private final ObjectMapper mapper = new ObjectMapper();

    private final RestTemplate restTemplate;

    private final SubscribableChannel imageQueue;

    private final String apiKey;

    private String token;

    private final AtomicBoolean initDone  = new AtomicBoolean(false);

    public ImageFetcher(String apiKey, RestTemplate restTemplate, SubscribableChannel imageQueue) {
        this.restTemplate = restTemplate;
        this.imageQueue = imageQueue;
        this.apiKey = apiKey;
    }

    @SneakyThrows
    private void fetchAllAndSendToCache() {
        if (token == null) {
            token = getToken(apiKey);
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(IMAGES_URI)
                .queryParam("page", 1);

        ImagePage page;
        URI uri = uriBuilder.build().toUri();
        log.info("Image page: [{}]", uri.toString());
        RequestEntity<Void> getPageRequest = newImagePageRequest(uri);
        do {
            getPageRequest = newImagePageRequest(uri);
            ResponseEntity<ImagePage> resp;
            try {
                resp = restTemplate.exchange(getPageRequest, ImagePage.class);
            } catch (HttpClientErrorException.Unauthorized unauthorizedEx) {
                token = getToken(apiKey);
                getPageRequest = newImagePageRequest(uri);
                resp = restTemplate.exchange(getPageRequest, ImagePage.class);
            }

            page = resp.getBody();

            for (Picture picture : page.getPictures()) {
                Image image = fetchImage(picture);
                sendImage(image);
                Thread.sleep(100);
            }
            uri = uriBuilder
                    .replaceQueryParam("page", page.getPage() + 1)
                    .build()
                    .toUri();
            log.info("Image page: [{}]", uri.toString());
        } while (page.hasMore);
    }

    private RequestEntity<Void> newImagePageRequest(URI uri) {
        return RequestEntity
                .get(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
    }

    private void sendImage(Image image) {
        Message<Image> msg = MessageBuilder
                .withPayload(image)
                .build();
        imageQueue.send(msg);
    }

    private Image fetchImage(Picture picture) {
        RequestEntity<Void> req = RequestEntity.get(URI.create(IMAGES_URI + "/" + picture.getId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        return restTemplate.exchange(req, Image.class).getBody();
    }

    @SneakyThrows
    private String getToken(String apiKey) {
        ApiTokenRequest request = new ApiTokenRequest(apiKey);
        String body = mapper.writeValueAsString(request);


        RequestEntity<String> reqEntity = RequestEntity.post(URI.create("http://interview.agileengine.com/auth"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);

        final ApiTokenResponse resp = restTemplate.exchange(reqEntity, ApiTokenResponse.class).getBody();
        return resp.getToken();
    }


    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        fetchAllAndSendToCache();
        initDone.set(true);
    }

    public void fetchPeriodically() {
        if (initDone.get()) {
            fetchAllAndSendToCache();
        }
    }

    @Data
    private static class ImagePage {

        private List<Picture> pictures;

        private int page;

        private int pageCount;

        private boolean hasMore;
    }

    @Data
    private static class Picture {
        private String id;
    }

    @Data
    private static class ApiTokenRequest {
        private String apiKey;

        public ApiTokenRequest(String apiKey) {
            this.apiKey = apiKey;
        }
    }

    @Data
    private static class ApiTokenResponse {
        private String token;
        private boolean auth;
    }
}
