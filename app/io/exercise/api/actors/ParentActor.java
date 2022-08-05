package io.exercise.api.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import play.libs.akka.InjectedActorSupport;

import javax.inject.Inject;
/**
 * Created by agonlohaj on 04 Sep, 2020
 */
public class ParentActor extends AbstractActor implements InjectedActorSupport {

	private ConfiguredChildActorProtocol.Factory childFactory;

	@Inject
	public ParentActor(ConfiguredChildActorProtocol.Factory childFactory) {
		this.childFactory = childFactory;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(ParentActorProtocol.GetChild.class, this::getChild).build();
	}

	private void getChild(ParentActorProtocol.GetChild msg) {
		String key = msg.getKey();
		ActorRef child = injectedChild(() -> childFactory.create(key), key);
		sender().tell(child, self());
	}
}
