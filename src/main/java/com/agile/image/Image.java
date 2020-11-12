package com.agile.image;

/*{
        "id": "213d806b28c4e00bb3b2",
        "author": "Stained Card",
        "camera": "Canon EOS Rebel SL3 / EOS 250D",
        "tags": "#photooftheday #life #wonderfulday #whataview #nature #wonderfullife #greatview ",
        "cropped_picture": "http://interview.agileengine.com/pictures/cropped/0002.jpg",
        "full_picture": "http://interview.agileengine.com/pictures/full_size/0002.jpg"
        }
*/

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Image {

    private String id;

    private String author;

    private String camera;

    private String tags;

    @JsonProperty("cropped_picture")
    private String croppedPicture;

    @JsonProperty("full_picture")
    private String fullPicture;


}
