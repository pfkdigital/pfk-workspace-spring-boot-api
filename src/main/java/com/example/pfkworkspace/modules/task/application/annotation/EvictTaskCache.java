package com.example.pfkworkspace.modules.task.application.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.cache.annotation.CacheEvict;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@CacheEvict(value = "tasks", key = "#workspaceId + '_' + #projectId + '_' + #taskId")
public @interface EvictTaskCache {}
