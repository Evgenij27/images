package com.agile.image;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ImageIndexService {

    private final Map<String, Set<String>> authorIndex = new ConcurrentHashMap<>();

    private final Map<String, Set<String>> cameraIndex = new ConcurrentHashMap<>();

    private final Map<String, Set<String>> tagIndex = new ConcurrentHashMap<>();

    public void index(Image image) {
        indexAuthor(image);
        indexCamera(image);
        indexTags(image);
    }

    private void indexAuthor(Image image) {
        if (image.getAuthor() != null) {
            authorIndex.computeIfAbsent(image.getAuthor(), k -> new TreeSet<>())
                    .add(image.getId());
        }

    }

    private void indexCamera(Image image) {
        if (image.getCamera() != null) {
            cameraIndex.computeIfAbsent(image.getCamera(), k -> new TreeSet<>())
                    .add(image.getId());
        }
    }

    private void indexTags(Image image) {
        if (image.getTags() != null) {
            String[] tags = image.getTags().split(" ");
            for (String tag : tags) {
                tagIndex.computeIfAbsent(tag, k -> new TreeSet<>())
                        .add(image.getId());
            }
        }
    }

    public Set<String> findByTerm(String term) {
        Set<String> result = new TreeSet<>();
        Set<String> ids = authorIndex.get(term);
        if (ids != null) {
            result.addAll(ids);
        }

        ids = cameraIndex.get(term);
        if (ids != null) {
            result.addAll(ids);
        }

        ids = tagIndex.get("#".concat(term));
        if (ids != null) {
            result.addAll(ids);
        }

        return result;
    }
}
