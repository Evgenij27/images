package com.agile.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageCacheService {

    private final ConcurrentHashMap<String, Image> images = new ConcurrentHashMap<>();

    private final ImageIndexService indexService;

    @Autowired
    public ImageCacheService(ImageIndexService indexService) {
        this.indexService = indexService;
    }

    @ServiceActivator(inputChannel = "imageQueue")
    public void add(Image image) {
        log.info("Received image with id [{}]", image.getId());
        images.put(image.getId(), image);
        indexService.index(image);
    }

    public Image get(String id) {
        return images.get(id);
    }

    public List<Image> findByTerm(String term) {
         return indexService.findByTerm(term)
                 .stream()
                 .map(this::get)
                 .filter(Objects::nonNull)
                 .collect(Collectors.toList());

    }

}
