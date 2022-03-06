package io.github.galaxyvn.ggarden.command.framework.annotation;

import io.github.galaxyvn.ggarden.command.framework.GalaxyCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a method in a {@link GalaxyCommand} as an executable command
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GalaxyExecutable {
}
