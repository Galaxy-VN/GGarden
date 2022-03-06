package io.github.galaxyvn.ggarden.command.framework.annotation;

import io.github.galaxyvn.ggarden.command.framework.GalaxySubCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a {@link GalaxyExecutable} method parameter in a {@link GalaxySubCommand} as inherited from the parent command
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
