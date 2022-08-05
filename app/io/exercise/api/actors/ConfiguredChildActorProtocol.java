package io.exercise.api.actors;

import akka.actor.Actor;

/**
 * Created by agonlohaj on 04 Sep, 2020
 */
public class ConfiguredChildActorProtocol {

	public static class GetConfig {}

	public interface Factory {
		public Actor create(String key);
	}
}