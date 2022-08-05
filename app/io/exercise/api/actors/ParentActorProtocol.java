package io.exercise.api.actors;

import lombok.Data;

/**
 * Created by agonlohaj on 04 Sep, 2020
 */
public class ParentActorProtocol {

	@Data
	public static class GetChild {
		private String key;
	}
}