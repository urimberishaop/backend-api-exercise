package io.exercise.api.modules;

import com.google.inject.AbstractModule;

public class StartupModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(StartupProvider.class).asEagerSingleton();
    }
}