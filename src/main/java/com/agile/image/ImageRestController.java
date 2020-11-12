package com.agile.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ImageRestController {

    private final ImageCacheService cacheService;

    @Autowired
    public ImageRestController(ImageCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/search/{searchTerm}")
    public ResponseEntity<?> search(@PathVariable("searchTerm") String searchTerm) {
        List<Image> images = cacheService.findByTerm(searchTerm);
        if (images.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(images);
    }
}

