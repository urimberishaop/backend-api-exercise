package io.exercise.api.modules;

import com.google.inject.AbstractModule;
import io.exercise.api.actors.ConfiguredActor;
import io.exercise.api.actors.ConfiguredChildActor;
import io.exercise.api.actors.ConfiguredChildActorProtocol;
import io.exercise.api.actors.ParentActor;
import play.libs.akka.AkkaGuiceSupport;

public class ActorModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    protected void configure() {
        bindActor(ConfiguredActor.class, "configured-actor");
        bindActor(ParentActor.class, "parent-actor");
        bindActorFactory(ConfiguredChildActor.class, ConfiguredChildActorProtocol.Factory.class);
    }
}