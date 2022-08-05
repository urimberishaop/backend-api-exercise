package io.exercise.api.actors;
import akka.actor.AbstractActor;
import com.typesafe.config.Config;

import javax.inject.Inject;
/**
 * Created by agonlohaj on 04 Sep, 2020
 */
public class ConfiguredActor extends AbstractActor {

	private Config configuration;

	@Inject
	public ConfiguredActor(Config configuration) {
		this.configuration = configuration;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(ConfiguredActorProtocol.GetConfig.class, message -> {
				sender().tell(configuration.getString("environment"), self());
			})
			.build();
	}
}