package io.exercise.api.actors;

import akka.actor.AbstractActor;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;

import javax.inject.Inject;
/**
 * Created by agonlohaj on 04 Sep, 2020
 */
public class ConfiguredChildActor extends AbstractActor {

	private final Config configuration;
	private final String key;

	@Inject
	public ConfiguredChildActor(Config configuration, @Assisted String key) {
		this.configuration = configuration;
		this.key = key;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(ConfiguredChildActorProtocol.GetConfig.class, this::getConfig)
				.build();
	}

	private void getConfig(ConfiguredChildActorProtocol.GetConfig get) {
		sender().tell(configuration.getString(key), self());
	}
}
