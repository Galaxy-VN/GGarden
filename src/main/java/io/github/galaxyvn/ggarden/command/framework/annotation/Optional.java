package io.github.galaxyvn.ggarden.command.framework.annotation;

import io.github.galaxyvn.ggarden.command.framework.GalaxyCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a {@link GalaxyExecutable} method parameter in a {@link GalaxyCommand} as optional
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Optional {
}
