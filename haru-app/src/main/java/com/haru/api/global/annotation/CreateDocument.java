package com.haru.api.global.annotation;

import com.haru.api.user.domain.enums.DocumentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CreateDocument {
    DocumentType documentType();
}
