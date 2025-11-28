package com.sribalajiads.media_app.dto;

import lombok.Data;

@Data // Lombok annotation to generate getters, setters, constructor, etc.
public class CreateMediaRequest {

    private String mediaCode;
    private String location;
    private String city;
    private String specifications;
    private String illumination;
    private String mediaType;

    // We don't include the 'image' here because it will be handled as a separate part of the multipart request.
}