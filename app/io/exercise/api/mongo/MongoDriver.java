package io.exercise.api.mongo;

import akka.Done;
import akka.actor.CoordinatedShutdown;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import io.exercise.api.models.*;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import play.Logger;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;

/**
 * Created by Agon on 09/08/2020.
 */
public abstract class MongoDriver implements IMongoDB {
	protected final Config config;
	protected MongoClient client;
	private MongoDatabase database;

	protected MongoDriver(CoordinatedShutdown coordinatedShutdown, Config config) {
		this.config = config;

		coordinatedShutdown.addTask(CoordinatedShutdown.PhaseServiceStop(), "shutting-down-mongo-connections", () -> {
			Logger.of(this.getClass()).debug("Shutting down mongo connections!");
			close();
			return CompletableFuture.completedFuture(Done.done());
		});
	}

	/**
	 * Get a mongo database connection if not already available
	 * @return
	 */
	public synchronized MongoDatabase getMongoDatabase() {
		if (database == null) {
			database = this.connect();
		}

		ClassModel<Content> contentModel = ClassModel.builder(Content.class).enableDiscriminator(true).build();
		ClassModel<ImageContent> imageContentModel = ClassModel.builder(ImageContent.class).enableDiscriminator(true).build();
		ClassModel<TextContent> textContentModel = ClassModel.builder(TextContent.class).enableDiscriminator(true).build();
		ClassModel<EmailContent> emailContentModel = ClassModel.builder(EmailContent.class).enableDiscriminator(true).build();
		ClassModel<Dashboard> dashboard = ClassModel.builder(Dashboard.class).enableDiscriminator(false).build();

		CodecProvider pojoCodecProvider =
				PojoCodecProvider.builder()
						.conventions(Collections.singletonList(ANNOTATION_CONVENTION))
						.register("io.exercise.api.models")
						.register(dashboard, contentModel, imageContentModel, textContentModel, emailContentModel)
						.automatic(true)
						.build();

		final CodecRegistry customEnumCodecs = CodecRegistries.fromCodecs();
		CodecRegistry pojoCodecRegistry = CodecRegistries
			.fromRegistries(
				MongoClientSettings.getDefaultCodecRegistry(),
				customEnumCodecs,
				CodecRegistries.fromProviders(pojoCodecProvider)
			);

		return database.withCodecRegistry(pojoCodecRegistry);
	}

	protected abstract MongoDatabase connect();

	protected abstract void disconnect();

	public MongoClient getMongoClient() {
		return client;
	}

	/**
	 * Shut down database connections when the app stops
	 */
	private void close() {
		if (database != null) {
			database = null;
		}
		disconnect();
	}
}
