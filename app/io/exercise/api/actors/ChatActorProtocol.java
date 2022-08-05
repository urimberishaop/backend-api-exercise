package io.exercise.api.actors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by agonlohaj on 04 Sep, 2020
 */
public class ChatActorProtocol {

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ChatMessage implements ActorMessage {
		private String message;
	}
}